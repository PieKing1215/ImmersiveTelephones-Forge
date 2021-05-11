package me.pieking1215.immersive_telephones.mixin.client;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.Client;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(Client.class)
public interface MixinClientAccessor {

    @Accessor(value = "audioChannels", remap = false)
    Map<UUID, AudioChannel> getAudioChannels();

}
