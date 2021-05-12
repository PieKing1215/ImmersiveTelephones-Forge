package me.pieking1215.immersive_telephones.mixin.client;

import de.maxhenkel.voicechat.voice.client.AudioChannel;
import me.pieking1215.immersive_telephones.client.voice.TelephoneAudioChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(AudioChannel.class)
class MixinAudioChannel {

    @Inject(method = "writeToSpeaker", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectWriteToSpeaker(byte[] monoData, CallbackInfo ci){
        AudioChannel origac = (AudioChannel)(Object)this;
        if(origac instanceof TelephoneAudioChannel){
            TelephoneAudioChannel ac = (TelephoneAudioChannel)origac;
            ac.customWriteToSpeaker(monoData);
            ci.cancel();
        }
    }

}
