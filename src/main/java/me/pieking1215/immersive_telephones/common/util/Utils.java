package me.pieking1215.immersive_telephones.common.util;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

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
            return findSwitchboards(te).anyMatch(sb -> sb.isFunctional(type, obj));
        }

        // the goal is to never get here but that cannot be assumed

        return true; // allow things to work if we don't know how to handle them
    }

    public static Stream<BaseSwitchboardTileEntity> findSwitchboards(TileEntity teIn){
        if(teIn.getWorld() == null) return Stream.empty();

        List<LocalWireNetwork> nets;

        LocalWireNetwork net = GlobalWireNetwork.getNetwork(teIn.getWorld()).getNullableLocalNet(teIn.getPos());
        if (net == null) {
            nets = Arrays.stream(Direction.values()).map(d -> GlobalWireNetwork.getNetwork(teIn.getWorld()).getNullableLocalNet(teIn.getPos().offset(d)))
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }else{
            nets = Collections.singletonList(net);
        }

        return nets.stream().map(LocalWireNetwork::getConnectors).flatMap(Collection::stream).map(p -> {
            TileEntity te = teIn.getWorld().getTileEntity(p);
            if(!(te instanceof EnergyConnectorTileEntity)) return null;

            BlockPos p2 = p.offset(((EnergyConnectorTileEntity)te).getFacing());
            TileEntity te2 = teIn.getWorld().getTileEntity(p2);
            if(!(te2 instanceof BaseSwitchboardTileEntity)) return null;

            return (BaseSwitchboardTileEntity) te2;
        }).filter(Objects::nonNull);
    }

}
