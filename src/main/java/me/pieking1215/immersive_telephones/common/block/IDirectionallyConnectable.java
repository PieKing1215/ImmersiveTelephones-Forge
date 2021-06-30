package me.pieking1215.immersive_telephones.common.block;

import net.minecraft.util.Direction;

public interface IDirectionallyConnectable {
    default boolean canConnect(Direction dir){
        return true;
    }
}
