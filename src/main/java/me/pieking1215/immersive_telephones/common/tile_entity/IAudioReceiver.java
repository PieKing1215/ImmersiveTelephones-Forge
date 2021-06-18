package me.pieking1215.immersive_telephones.common.tile_entity;

import de.maxhenkel.voicechat.voice.common.MicPacket;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public interface IAudioReceiver {

    UUID getReceiverUUID();

    Vector3d getReceiverPos();

    // server side
    void recieveAudio(MicPacket packet);

}
