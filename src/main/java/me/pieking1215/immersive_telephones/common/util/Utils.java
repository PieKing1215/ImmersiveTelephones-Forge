package me.pieking1215.immersive_telephones.common.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

}
