package me.pieking1215.immersive_telephones.common.tile_entity;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ConnectorTileHelper;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BasePhoneTileEntity extends TileEntity implements IImmersiveConnectable, ITickableTileEntity, ICallable, IAudioReceiver, IAudioProvider, IAudioPlayerHandler {
    private GlobalWireNetwork globalNet;

    private UUID tel_UUID;
    private String number = "000";

    protected ICallable whoRings = null; // server only
    protected long ringTime = -1;

    // TODO: I think storing TEs like this might not be safe
    //       since if it unloads it'll be invalid
    protected final List<ICallable> inCallWith = new ArrayList<>();
    protected boolean inCall = false;

    public BasePhoneTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        tel_UUID = UUID.randomUUID();
    }

    //region <IImmersiveConnectable>

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public BlockPos getConnectionMaster(@Nullable WireType wireType, TargetingInfo targetingInfo) {
        return this.getPos();
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vector3i vector3i) {
        return true;
    }

    @Override
    public void connectCable(WireType wireType, ConnectionPoint connectionPoint, IImmersiveConnectable iImmersiveConnectable, ConnectionPoint connectionPoint1) {

    }

    @Nullable
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo targetingInfo, Vector3i vector3i) {
        return new ConnectionPoint(this.pos, 0);
    }

    @Override
    public void removeCable(@Nullable Connection connection, ConnectionPoint connectionPoint) {
        this.markDirty();
    }

    @Override
    public Vector3d getConnectionOffset(@Nonnull Connection connection, ConnectionPoint connectionPoint) {
        Direction side = getBlockState().get(TelephoneBlock.FACING);
        return new Vector3d(0.5 + side.getXOffset() * (7.0/16.0), 15.0 / 16.0, 0.5 + side.getZOffset() * (7.0/16.0));
    }

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableList.of(new ConnectionPoint(this.pos, 0));
    }

    //endregion

    public List<ICallable> findConnectedCallables(){
        List<ICallable> list = new ArrayList<>();

        if(world == null) return list;

        LocalWireNetwork net = GlobalWireNetwork.getNetwork(this.world).getNullableLocalNet(this.getPos());
        if (net == null) return list;

        for(BlockPos p : net.getConnectors()){
            IImmersiveConnectable connect = net.getConnector(p);
            if(connect instanceof ICallable && connect != this){
                list.add((ICallable) connect);
            }
        }

        return list;
    }

    @Override
    public void setWorldAndPos(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        super.setWorldAndPos(worldIn, pos);
        this.globalNet = GlobalWireNetwork.getNetwork(worldIn);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        globalNet.onConnectorUnload(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Preconditions.checkNotNull(world);

        GlobalWireNetwork.getNetwork(world).onConnectorLoad(this, world);
    }

    @Nonnull
    public IModelData getModelData() {
        Preconditions.checkNotNull(world);

        return CombinedModelData.combine(new SinglePropertyModelData<>(ConnectorTileHelper.genConnBlockState(this.world, this), IEProperties.Model.CONNECTIONS), super.getModelData());
    }

    @Override
    public void remove() {
        Preconditions.checkNotNull(world);

        super.remove();
        ConnectorTileHelper.remove(this.world, this);
    }

    //region <networking>

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putUniqueId("tel_UUID", tel_UUID);
        nbt.putString("name", number);

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        number = nbt.getString("name");
        if(nbt.contains("tel_UUID")) tel_UUID = nbt.getUniqueId("tel_UUID");
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putUniqueId("tel_UUID", tel_UUID);
        nbt.putString("name", number);
        nbt.putLong("ringTime", ringTime);
        nbt.putBoolean("inCall", inCall);

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        tel_UUID = nbt.getUniqueId("tel_UUID");
        number = nbt.getString("name");
        ringTime = nbt.getLong("ringTime");
        inCall = nbt.getBoolean("inCall");
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

    // endregion

    public UUID getUUID() {
        return tel_UUID;
    }

    @Override
    public void tick() {
        Preconditions.checkNotNull(world);

        if(!world.isRemote) {
            // server side

            if(inCall){
                if(world.getGameTime() % 10 == 0){

                    ServerWorld sw = (ServerWorld) world;

                    // green particle line between phones in a call together
                    Vector3d from = new Vector3d(getPos().getX(), getPos().getY(), getPos().getZ());
                    for(ICallable other : inCallWith) {
                        if(other instanceof TileEntity){
                            TileEntity ot = (TileEntity) other;
                            for (float t = 0; t < 1.0f; t += 0.1f) {
                                Vector3d v3 = from.add((new Vector3d(ot.getPos().getX(), ot.getPos().getY(), ot.getPos().getZ()).subtract(from)).mul(t, t, t));
                                v3 = v3.add(0.5, 0.5, 0.5);

                                sw.spawnParticle(ParticleTypes.HAPPY_VILLAGER, v3.x, v3.y, v3.z, 1, 0, 0, 0, 0.0f);
                            }
                        }
                    }

                    // hearts above this phone
                    sw.spawnParticle(ParticleTypes.HEART, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, 1, 0, 0, 0, 0.0f);

                }
            }

            if(whoRings != null && (!isRinging() || !whoRings.isStillCalling(this))){
                onRingingCancelled();
            }

        }
    }

    @Override
    public UUID getReceiverUUID() {
        return getUUID();
    }

    @Override
    public Vector3d getReceiverPos() {
        return Vector3d.copyCentered(getPos());
    }

    @Override
    public void onDialed(ICallable dialedBy) {
        Preconditions.checkNotNull(world);

        if(!world.isRemote){
            // server side

            this.ringTime = world.getGameTime() + 20 * 10;
            this.whoRings = dialedBy;

            world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    public boolean isStillCalling(ICallable other) {
        return true; //TODO: actually keep track of who I'm dialing
    }

    @Override
    public void onAnsweredCall(ICallable answerer) {
        // answerer is the other device which accepted our call
        inCallWith.forEach(o -> {
            answerer.onAddedToCall(this, o);
            o.onAddedToCall(this, answerer);
        });
        addToCall(answerer);
    }

    @Override
    public void onAddedToCall(ICallable whoAdded, ICallable added) {
        // any random phone shouldn't be allowed to add itself
        if(whoAdded != this && inCallWith.contains(whoAdded)){
            addToCall(added);
        }
    }

    @Override
    public void onLeftCall(ICallable leaver) {
        inCallWith.remove(leaver);

        if(inCallWith.isEmpty()){
            inCall = false;
            // don't reset interactingPlayer since out player hasn't hung up yet
        }
    }

    @Override
    public String getID() {
        return number;
    }

    protected void onRingingCancelled(){
        whoRings = null;
    }

    // player could be null if a particular subclass wants to have an automatic answer or something
    public void answerPhone(@Nullable ServerPlayerEntity player) {
        // server side

        Preconditions.checkNotNull(world);

        ringTime = -1;

        if(whoRings.isStillCalling(this)) {
            inCall = true;
            inCallWith.add(whoRings);
            whoRings.onAnsweredCall(this);

            inCallWith.forEach(o -> {
                whoRings.onAddedToCall(this, o);
                o.onAddedToCall(this, whoRings);

                inCallWith.forEach(o2 -> {
                    o.onAddedToCall(this, o2);
                    o2.onAddedToCall(this, o);
                });

            });

            whoRings = null;
        }

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @SuppressWarnings("WeakerAccess")
    public void addToCall(ICallable other){
        Preconditions.checkNotNull(world);

        if(other == this || inCallWith.contains(other)){
            // bonus check because I definitely got the logic in answerPhone wrong
            return;
        }

        inCall = true;
        inCallWith.add(other);

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void setNumber(String displayName) {
        this.number = displayName;
    }

    public void dial(String id){
        Utils.findSwitchboards(this).map(sb -> sb.findCallable(id))
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst().ifPresent(other -> other.onDialed(this));
    }

    @SuppressWarnings("WeakerAccess")
    public long getRingTime(){
        return ringTime;
    }

    public boolean isRinging() {
        if(world == null) return false;
        return getRingTime() > world.getGameTime();
    }

    public ICallable getWhoRings(){
        return whoRings;
    }

    public List<ICallable> getInCallWith(){
        return inCallWith;
    }

    public void endCall(){
        Preconditions.checkNotNull(world);

        inCall = false;

        for(ICallable ca : inCallWith){
            ca.onLeftCall(this);
        }

        inCallWith.clear();

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, true));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    public Collection<IAudioReceiver> getRecievers(PlayerEntity source) {
        return inCallWith.stream().filter(t -> t instanceof IAudioReceiver).map(t -> (IAudioReceiver)t).collect(Collectors.toList());
    }

    @Override
    public UUID getChannelUUID() {
        return tel_UUID;
    }

    @Override
    public Vector3d getPlaybackPosition() {
        return Vector3d.copyCentered(pos);
    }

    @Override
    public boolean shouldBeMono() {
        return false;
    }
}
