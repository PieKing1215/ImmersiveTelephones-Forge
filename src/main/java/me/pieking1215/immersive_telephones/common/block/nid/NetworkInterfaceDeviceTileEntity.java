package me.pieking1215.immersive_telephones.common.block.nid;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import com.google.common.base.Preconditions;
import mcp.MethodsReturnNonnullByDefault;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.ICallable;
import me.pieking1215.immersive_telephones.common.block.IDirectionallyConnectable;
import me.pieking1215.immersive_telephones.common.block.IHasID;
import me.pieking1215.immersive_telephones.common.block.TileEntityRegister;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardTileEntity;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkInterfaceDeviceTileEntity extends BaseSwitchboardTileEntity implements ICallable, IDirectionallyConnectable {

    private String number = "000";

    public NetworkInterfaceDeviceTileEntity() {
        super(TileEntityRegister.NETWORK_INTERFACE_DEVICE.get());
        routerCapacity = 0;
    }

    protected ICallable whoRings = null;

    //region <networking>

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putString("name", number);

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        number = nbt.getString("name");

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putString("name", number);

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        number = nbt.getString("name");

        Preconditions.checkNotNull(world);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(getBlockState(), pkt.getNbtCompound());
    }

    //endregion

    @Override
    public void onDialed(ICallable dialedBy, String query) {
        Preconditions.checkNotNull(world);
        Preconditions.checkState(!world.isRemote);

        // if there are two nested local networks, this would prevent the inner one from being
        //     triggered when the outer one is dialed
        // TODO: this could be configurable per-instance
        //if(!matches(query)) return;

        List<ICallable> localNetwork = getLocalNetwork();

        boolean isFromLocal = localNetwork.stream().anyMatch(c -> c == dialedBy);

        Optional.ofNullable(world.getClosestPlayer(EntityPredicate.DEFAULT, getPos().getX(), getPos().getY(), getPos().getZ()))
                .ifPresent(p -> p.sendMessage(new StringTextComponent(getPos().toString() + " : onDialed " + (isFromLocal ? "local" : "external")), Util.DUMMY_UUID));

        ServerWorld sw = (ServerWorld)world;
        sw.spawnParticle(ParticleTypes.HAPPY_VILLAGER, getPos().getX(), getPos().getY() + 1, getPos().getZ(), 1, 0, 0, 0, 0.0f);

        if(!isFromLocal){
            String prefix = getID() + "+";
            if(query.startsWith(prefix)) {
                String internal = query.substring(prefix.length());
                localNetwork.stream().filter(c -> c.matches(internal)).forEach(c -> c.onDialed(dialedBy, internal));
            }else {
                // note: the query passed to the device does not necessarily match its id
                //     (ex c.matches(query) is not necessarily true)
                // TODO: this can kind of be used to block non-specific access to a certain internal device,
                //           but won't work if the nid's id is the same as the device's id
                localNetwork.forEach(c -> c.onDialed(dialedBy, query));
            }

            whoRings = dialedBy;
        }else{
            String prefix = getID() + "+";
            if(query.startsWith(prefix)) {
                String internal = query.substring(prefix.length());
                localNetwork.stream().filter(c -> c.matches(internal)).forEach(c -> {
                    if(c != dialedBy)
                        c.onDialed(dialedBy, internal);
                });
            }else if(query.startsWith("+")) { // local shortcut
                String internal = query.substring(1);
                localNetwork.stream().filter(c -> c.matches(internal)).forEach(c -> {
                    if(c != dialedBy)
                        c.onDialed(dialedBy, internal);
                });
            }else {
                // note: the query passed to the device does not necessarily match its id
                //     (ex c.matches(query) is not necessarily true)
                // TODO: this can kind of be used to block non-specific access to a certain internal device,
                //           but won't work if the nid's id is the same as the device's id
                localNetwork.forEach(c -> {
                    if(c != dialedBy)
                        c.onDialed(dialedBy, query);
                });
            }

            whoRings = dialedBy;
        }

    }

    protected List<ICallable> getLocalNetwork(){
        Preconditions.checkNotNull(world);
        Direction facing = getBlockState().get(NetworkInterfaceDeviceBlock.FACING);
        Collection<ConnectionPoint> net = GlobalWireNetwork.getNetwork(world).getNullableLocalNet(getPos().offset(facing.rotateY())).getConnectionPoints();

        return net.stream().map(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if (te != null) {
                if (te instanceof ICallable) {
                    return (ICallable) te;
                } else if (te instanceof EnergyConnectorTileEntity) {
                    EnergyConnectorTileEntity conn = (EnergyConnectorTileEntity) te;
                    TileEntity te2 = world.getTileEntity(c.getPosition().offset(conn.getFacing()));
                    if (te2 instanceof ICallable
                            && !(te2 instanceof IDirectionallyConnectable && !((IDirectionallyConnectable) te2).canConnect(conn.getFacing().getOpposite()))) {
                        return (ICallable) te2;
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).filter(c -> c != this).collect(Collectors.toList());
    }

    @Override
    public boolean isStillCalling(ICallable other) {
        return whoRings.isStillCalling(this);
    }

    @Override
    public void onAnsweredCall(ICallable answerer) {
        whoRings.onAnsweredCall(this);
    }

    @Override
    public void onAddedToCall(ICallable whoAdded, ICallable added) {

    }

    @Override
    public void onLeftCall(ICallable leaver) {
        whoRings.onLeftCall(this);
    }

    public void setNumber(String num){
        number = num;
    }

    @Override
    public String getID() {
        return number;
    }

    @Override
    public boolean canConnect(Direction dir) {
        Direction facing = getBlockState().get(NetworkInterfaceDeviceBlock.FACING);
        return dir == facing.rotateYCCW(); // act as device to external network
    }

    @Override
    public boolean isSwitchboardOnSide(Direction dir) {
        Direction facing = getBlockState().get(NetworkInterfaceDeviceBlock.FACING);
        return dir == facing.rotateY(); // act as switchboard to internal network
    }

    @Override
    public <T> int getCapacityForType(Class<T> type) {
        return 100; // TODO
    }

    @Override
    public <T extends IHasID> Optional<T> findTileEntitiesWithType(Class<T> type, String id) {

        // shortcut for local network
        // TODO: this trick doesn't work for nested nids
        //           it would need another plus at the start
        if(id.startsWith("+")) id = getID() + id;

        String finalId = id;
        return Utils.findSwitchboards(world, getPos().offset(getBlockState().get(NetworkInterfaceDeviceBlock.FACING).rotateYCCW()))
                .map(sb -> sb.findTileEntitiesWithType(type, finalId)).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    @Override
    public boolean matches(String query) {

        // allow dialing through to specific internal devices
        // ie if this nid is 000 and has devices 111 and 222 internally,
        //     an external device could dial 000+111
        // TODO: this could be configurable per-instance
        String prefix = getID() + "+";
        if(query.startsWith(prefix)) {
            String internal = query.substring(prefix.length());
            List<ICallable> localNetwork = getLocalNetwork();
            return localNetwork.stream().anyMatch(c -> c.matches(internal));
        }

        return ICallable.super.matches(query);
    }
}
