package me.pieking1215.immersive_telephones.common.block.switchboard;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.common.block.router.ICapacityHandler;
import me.pieking1215.immersive_telephones.common.block.ICallable;
import me.pieking1215.immersive_telephones.common.block.IHasID;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BaseSwitchboardTileEntity extends TileEntity {

    protected int routerCapacity = 0;

    public BaseSwitchboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public Optional<ICallable> findCallable(String id){
        return findTileEntitiesWithType(ICallable.class, id);
    }

    public <T extends IHasID> Optional<T> findTileEntitiesWithType(Class<T> type, String id){
        Preconditions.checkNotNull(world);

        Collection<ConnectionPoint> net = scanConnectedNetworks();
        AtomicBoolean stopCounting = new AtomicBoolean(false);
        int skip = net.stream().map(c -> {
            if(stopCounting.get()) return null; // if already found self in the stream

            TileEntity te = world.getTileEntity(c.getPosition());
            if(te instanceof EnergyConnectorTileEntity){
                BlockPos p2 = c.getPosition().offset(((EnergyConnectorTileEntity) te).getFacing());
                TileEntity te2 = world.getTileEntity(p2);

                if(te2 == this) {
                    stopCounting.set(true);
                    return null;
                }

                if(te2 instanceof BaseSwitchboardTileEntity){
                    return (BaseSwitchboardTileEntity)te2;
                }
            }
            return null;
        }).filter(Objects::nonNull).distinct().map(sb -> sb.getCapacityForType(type)).reduce(Integer::sum).orElse(0);

        return net.stream().map(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te != null) {
                if (type.isAssignableFrom(te.getClass())) {
                    //noinspection unchecked
                    return (T) te;
                } else if (te instanceof EnergyConnectorTileEntity) {
                    EnergyConnectorTileEntity conn = (EnergyConnectorTileEntity) te;
                    TileEntity te2 = world.getTileEntity(c.getPosition().offset(conn.getFacing()));
                    if (te2 != null && type.isAssignableFrom(te2.getClass())) {
                        //noinspection unchecked
                        return (T) te2;
                    }
                }
            }
            return null;
        }).filter(Objects::nonNull).skip(skip).limit(getCapacityForType(type)).filter(c -> c.getID().equals(id)).findFirst();
    }

    public <T> int getCapacityForType(Class<T> type){
        Preconditions.checkNotNull(world);

        int sum = 0;
        for(int i = 0; i < routerCapacity; i++) {
            BlockPos pos = getPos().offset(getBlockState().get(BaseSwitchboardBlock.FACING), i + 1);

            TileEntity te = world.getTileEntity(pos);
            if(te instanceof ICapacityHandler){
                sum += ((ICapacityHandler) te).getCapacity(type);
            }

            if(world.getBlockState(pos).getBlock() instanceof ICapacityHandler){
                sum += ((ICapacityHandler) world.getBlockState(pos).getBlock()).getCapacity(type);
            }

        }

        return sum;
    }

    public <T> boolean isFunctional(Class<T> type, T obj){
        Preconditions.checkNotNull(world);

        Collection<ConnectionPoint> net = scanConnectedNetworks();
        int sumCapacity = net.stream().map(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te instanceof EnergyConnectorTileEntity){
                BlockPos p2 = c.getPosition().offset(((EnergyConnectorTileEntity) te).getFacing());
                TileEntity te2 = world.getTileEntity(p2);

                if(te2 instanceof BaseSwitchboardTileEntity){
                    return (BaseSwitchboardTileEntity)te2;
                }
            }
            return null;
        }).filter(Objects::nonNull).distinct().map(sb -> sb.getCapacityForType(type)).reduce(Integer::sum).orElse(0);

        // find index of obj in net
        boolean found = false;
        int index = 0;
        for (ConnectionPoint c : net) {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te != null) {
                if (type.isAssignableFrom(te.getClass())) {
                    if (te == obj) {
                        found = true;
                        break;
                    }
                    index++;
                }else if(te instanceof EnergyConnectorTileEntity){
                    EnergyConnectorTileEntity conn = (EnergyConnectorTileEntity) te;
                    TileEntity te2 = world.getTileEntity(c.getPosition().offset(conn.getFacing()));
                    if(te2 != null && type.isAssignableFrom(te2.getClass())){
                        if (te2 == obj) {
                            found = true;
                            break;
                        }
                        index++;
                    }
                }
            }
        }

        if(found){
            return index < sumCapacity;
        }

        return false;
    }

    public Collection<ConnectionPoint> scanConnectedNetworks(){
        Preconditions.checkNotNull(world);

        // TODO: this won't work across chunk borders
        Stream<Collection<ConnectionPoint>> c = GlobalWireNetwork.getNetwork(world).getAllConnectorsIn(world.getChunkAt(getPos()).getPos())
                .stream().map(cnp -> {
                    TileEntity te = world.getTileEntity(cnp.getPosition());
                    if(!(te instanceof EnergyConnectorTileEntity)) return Collections.emptyList();

                    EnergyConnectorTileEntity conn = (EnergyConnectorTileEntity) te;
                    if(conn.getPos().offset(conn.getFacing()).equals(getPos())){

                        LocalWireNetwork net = GlobalWireNetwork.getNetwork(this.world).getNullableLocalNet(conn.getPos());
                        if (net == null) return Collections.emptyList();

                        return net.getConnectionPoints();

                    }

                    return Collections.emptyList();
                });
        return c.flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

}
