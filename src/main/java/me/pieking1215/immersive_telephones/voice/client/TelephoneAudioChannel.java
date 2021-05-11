package me.pieking1215.immersive_telephones.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.common.Utils;
import me.pieking1215.immersive_telephones.mixin.client.MixinAudioChannelAccessor;
import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;

public class TelephoneAudioChannel extends AudioChannel {

    private BlockPos pos;
    private TelephoneTileEntity te;

    public TelephoneAudioChannel(Client client, UUID uuid, BlockPos position, TelephoneTileEntity te) {
        super(client, uuid);
        this.pos = position;
        this.te = te;
    }

    // if this could override AudioChannel::writeToSpeaker it would, but it's private
    // instead this is injected by MixinAudioChannel only for TelephoneAudioChannel
    public void customWriteToSpeaker(byte[] monoData) {
        MixinAudioChannelAccessor accessor = (MixinAudioChannelAccessor)this;

        Pair<Float, Float> pan = calcPan();
        float volume = calcVolume();

        byte[] stereoData = Utils.convertToStereo(monoData, volume * pan.getLeft(), volume * pan.getRight());

        float volumeDB = Utils.percentageToDB(Main.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * (float) Main.VOLUME_CONFIG.getVolume(accessor.getUuid()));
        volumeDB = MathHelper.clamp(volumeDB, accessor.getGainControl().getMinimum(), accessor.getGainControl().getMaximum());
        accessor.getGainControl().setValue(volumeDB);

        accessor.getSpeaker().write(stereoData, 0, stereoData.length);
        accessor.getSpeaker().start();
    }

    private Pair<Float, Float> calcPan(){
        if(shouldBeMono() || !Main.CLIENT_CONFIG.stereo.get()){
            return Pair.of(1f, 1f);
        }

        return Utils.getStereoVolume(Minecraft.getInstance(), getPlaybackPosition(), Main.SERVER_CONFIG.voiceChatDistance.get().floatValue());
    }

    private float calcVolume(){
        if(shouldBeMono()) return 1f;

        float distance = (float) getPlaybackPosition().distanceTo(Minecraft.getInstance().player.getPositionVec());
        // TODO: different fading curve for phone
        // concept: sneaking while talking makes you "whisper" so nearby people can't hear at all unless they hold the phone
        float fadeStart = Main.SERVER_CONFIG.voiceChatFadeDistance.get().floatValue();
        float fadeEnd = Main.SERVER_CONFIG.voiceChatDistance.get().floatValue();

        return MathHelper.clamp(1f - (distance - fadeStart) / (fadeEnd - fadeStart), 0f, 1f);
    }

    private Vector3d getPlaybackPosition(){
        if(te.getHandsetEntity() != null){
            return te.getHandsetEntity().getPositionVec();
        }

        return Vector3d.copyCentered(pos);
    }

    private boolean shouldBeMono(){
        return Minecraft.getInstance().player == te.getHandsetEntity();
    }

    @Override
    public boolean canKill() {
        if(Minecraft.getInstance().player == null) return true;
        return !Minecraft.getInstance().player.worldClient.isBlockLoaded(pos);
    }

    @Override
    public void closeAndKill() {
        super.closeAndKill();
    }
}
