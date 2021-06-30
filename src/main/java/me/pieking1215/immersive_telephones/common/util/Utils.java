package me.pieking1215.immersive_telephones.common.util;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.common.block.IDirectionallyConnectable;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    // modified from https://forums.minecraftforge.net/topic/74979-1144-rotate-voxel-shapes/?tab=comments#comment-391969
    private static VoxelShape rotateShape(Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { shape, VoxelShapes.empty() };

        int times = (to.getHorizontalIndex() - Direction.NORTH.getHorizontalIndex() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1],
                    VoxelShapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    public static Map<Direction, VoxelShape> generateRotatedShapes(VoxelShape shape) {
        Map<Direction, VoxelShape> shapes = new HashMap<>();

        Arrays.stream(Direction.values()).forEach(d -> shapes.put(d, rotateShape(d, shape)));

        return shapes;
    }

    public static <T> boolean isFunctionalityFunctional(Class<T> type, T obj){
        // TODO: this will need updating when non-TE callables are worked on
        if(obj instanceof TileEntity){
            TileEntity te = (TileEntity) obj;
            return findSwitchboards(te.getWorld(), te.getPos()).anyMatch(sb -> sb.isFunctional(type, obj));
        }

        // the goal is to never get here but that cannot be assumed

        return true; // allow things to work if we don't know how to handle them
    }

    public static Stream<BaseSwitchboardTileEntity> findSwitchboards(World world, BlockPos pos){
        if(world == null) return Stream.empty();

        List<LocalWireNetwork> nets;

        LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getNullableLocalNet(pos);
        if (net == null) {
            nets = Arrays.stream(Direction.values()).map(d -> GlobalWireNetwork.getNetwork(world).getNullableLocalNet(pos.offset(d)))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }else{
            nets = Collections.singletonList(net);
        }

        return nets.stream().map(LocalWireNetwork::getConnectors).flatMap(Collection::stream).map(p -> {
            TileEntity te = world.getTileEntity(p);
            if(!(te instanceof EnergyConnectorTileEntity)) return null;

            BlockPos p2 = p.offset(((EnergyConnectorTileEntity)te).getFacing());
            TileEntity te2 = world.getTileEntity(p2);
            if(!(te2 instanceof BaseSwitchboardTileEntity)) return null;

            if(!((BaseSwitchboardTileEntity) te2).isSwitchboardOnSide(((EnergyConnectorTileEntity)te).getFacing().getOpposite())) return null;

            return (BaseSwitchboardTileEntity) te2;
        }).filter(Objects::nonNull);
    }

    public static Collection<ConnectionPoint> scanConnectedNetworks(World world, BlockPos pos){
        Preconditions.checkNotNull(world);

        // TODO: this won't work across chunk borders
        Stream<Collection<ConnectionPoint>> c = GlobalWireNetwork.getNetwork(world).getAllConnectorsIn(world.getChunkAt(pos).getPos())
                .stream().map(cnp -> {
                    TileEntity te = world.getTileEntity(cnp.getPosition());
                    if(!(te instanceof EnergyConnectorTileEntity)) return Collections.emptyList();

                    EnergyConnectorTileEntity conn = (EnergyConnectorTileEntity) te;
                    if(conn.getPos().offset(conn.getFacing()).equals(pos)){

                        LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getNullableLocalNet(conn.getPos());
                        if (net == null) return Collections.emptyList();

                        return net.getConnectionPoints();

                    }

                    return Collections.emptyList();
                });
        return c.flatMap(Collection::stream).distinct().collect(Collectors.toList());
    }

}
