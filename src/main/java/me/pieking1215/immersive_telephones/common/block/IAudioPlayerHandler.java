package me.pieking1215.immersive_telephones.common.block;

import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public interface IAudioPlayerHandler {

    Vector3d getPlaybackPosition();

    boolean shouldBeMono();

    UUID getChannelUUID();

}
