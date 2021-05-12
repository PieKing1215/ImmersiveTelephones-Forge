package me.pieking1215.immersive_telephones.common.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Map;

public class Utils {

    // modified from https://pastebin.com/Cj26PggQ
    private static void rotateShape(Direction to, VoxelShape shape, Map<Direction, VoxelShape> shapes) {
        VoxelShape[] buffer = new VoxelShape[] { shape, VoxelShapes.empty() };

        int times = (to.getHorizontalIndex() - Direction.NORTH.getHorizontalIndex() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1],
                    VoxelShapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        shapes.put(to, buffer[0]);
    }

    // modified from https://pastebin.com/Cj26PggQ
    public static void generateRotatedShapes(VoxelShape shape, Map<Direction, VoxelShape> shapes) {
        for (Direction direction : Direction.values()) {
            rotateShape(direction, shape, shapes);
        }
    }

}
