package me.pieking1215.immersive_telephones.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.Client;
import me.pieking1215.immersive_telephones.client.ClientConfig;
import me.pieking1215.immersive_telephones.common.IProxy;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.mixin.client.MixinClientAccessor;
import me.pieking1215.immersive_telephones.client.screen.TelephoneScreen;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.client.voice.TelephoneAudioChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;

public class ClientProxy implements IProxy {
    @SuppressWarnings("unused")
    @Override
    public void openPhoneScreen(TelephoneTileEntity te) {
        TelephoneScreen screen = new TelephoneScreen(te);
        Minecraft.getInstance().displayGuiScreen(screen);
    }

    @Override
    public boolean shouldCancelClick(InputEvent.ClickInputEvent ev) {
        return ev.isAttack()
                && Minecraft.getInstance().objectMouseOver != null
                && Minecraft.getInstance().objectMouseOver.getType() == RayTraceResult.Type.ENTITY
                && ((EntityRayTraceResult)Minecraft.getInstance().objectMouseOver).getEntity() instanceof HandsetEntity;
    }

    @Override
    public void registerTelephoneAudioChannel(TelephoneTileEntity te) {
        Client c = Main.CLIENT_VOICE_EVENTS.getClient();
        if(c == null) return;

        TelephoneAudioChannel ch = new TelephoneAudioChannel(c, te.getUUID(), te.getPos(), te);
        ch.start();
        AudioChannel old = ((MixinClientAccessor)c).getAudioChannels().put(te.getUUID(), ch);
        if(old != null) old.closeAndKill();
    }

    @Override
    public void loadConfig() {
        ClientConfig.registerClothConfig();
    }
}
