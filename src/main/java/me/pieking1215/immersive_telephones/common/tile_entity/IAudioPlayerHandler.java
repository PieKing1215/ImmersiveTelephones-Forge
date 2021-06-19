package me.pieking1215.immersive_telephones.common.tile_entity;

import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public interface IAudioPlayerHandler {

    Vector3d getPlaybackPosition();

    boolean shouldBeMono();

    UUID getChannelUUID();

}
