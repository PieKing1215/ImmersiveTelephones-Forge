package me.pieking1215.immersive_telephones.common.tile_entity.switchboard;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardBlock;
import me.pieking1215.immersive_telephones.common.tile_entity.IHasID;
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
    public BaseSwitchboardTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
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

        System.out.println("!!! SKIP " + skip);

        return net.stream().map(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te != null && type.isAssignableFrom(te.getClass())){
                //noinspection unchecked
                return (T)te;
            }
            return null;
        }).filter(Objects::nonNull).skip(skip).limit(getCapacityForType(type)).filter(c -> c.getID().equals(id)).findFirst();
    }

    public <T extends IHasID> int getCapacityForType(Class<T> type){
        return 1;
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
