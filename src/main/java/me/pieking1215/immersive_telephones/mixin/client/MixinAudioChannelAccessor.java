package me.pieking1215.immersive_telephones.mixin.client;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.common.OpusDecoder;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

@Mixin(AudioChannel.class)
public interface MixinAudioChannelAccessor {

    // regex
    // private (.+) (.)(.+);
    // @Accessor(value = "$2$3")\n$1 get\U$2\E$3();\n\n@Accessor(value = "$2$3")\nvoid set\U$2\E$3($1 $2$3);\n

    @Accessor(value = "minecraft", remap = false)
    Minecraft getMinecraft();

    @Accessor(value = "minecraft", remap = false)
    void setMinecraft(Minecraft minecraft);

    @Accessor(value = "client", remap = false)
    Client getClient();

    @Accessor(value = "client", remap = false)
    void setClient(Client client);

    @Accessor(value = "uuid", remap = false)
    UUID getUuid();

    @Accessor(value = "uuid", remap = false)
    void setUuid(UUID uuid);

    @Accessor(value = "queue", remap = false)
    BlockingQueue<SoundPacket> getQueue();

    @Accessor(value = "queue", remap = false)
    void setQueue(BlockingQueue<SoundPacket> queue);

    @Accessor(value = "lastPacketTime", remap = false)
    long getLastPacketTime();

    @Accessor(value = "lastPacketTime", remap = false)
    void setLastPacketTime(long lastPacketTime);

    @Accessor(value = "speaker", remap = false)
    SourceDataLine getSpeaker();

    @Accessor(value = "speaker", remap = false)
    void setSpeaker(SourceDataLine speaker);

    @Accessor(value = "gainControl", remap = false)
    FloatControl getGainControl();

    @Accessor(value = "gainControl", remap = false)
    void setGainControl(FloatControl gainControl);

    @Accessor(value = "stopped", remap = false)
    boolean getStopped();

    @Accessor(value = "stopped", remap = false)
    void setStopped(boolean stopped);

    @Accessor(value = "decoder", remap = false)
    OpusDecoder getDecoder();

    @Accessor(value = "decoder", remap = false)
    void setDecoder(OpusDecoder decoder);

    @Accessor(value = "lastSequenceNumber", remap = false)
    long getLastSequenceNumber();

    @Accessor(value = "lastSequenceNumber", remap = false)
    void setLastSequenceNumber(long lastSequenceNumber);

}
