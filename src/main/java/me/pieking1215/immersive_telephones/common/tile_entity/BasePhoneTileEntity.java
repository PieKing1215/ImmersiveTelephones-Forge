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
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BasePhoneTileEntity extends TileEntity implements IImmersiveConnectable, ITickableTileEntity {
    private GlobalWireNetwork globalNet;
    private UUID tel_UUID;

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

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        if(nbt.contains("tel_UUID")) tel_UUID = nbt.getUniqueId("tel_UUID");
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putUniqueId("tel_UUID", tel_UUID);

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        tel_UUID = nbt.getUniqueId("tel_UUID");
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

    }
}
