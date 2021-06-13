package me.pieking1215.immersive_telephones.common.tile_entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IAudioReceiver extends IHasID {

    @Nullable
    World getReceiverWorld();

    UUID getReceiverUUID();

    BlockPos getReceiverPos();

}
