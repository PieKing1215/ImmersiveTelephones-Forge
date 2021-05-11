package me.pieking1215.immersive_telephones.proxy;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.AudioChannel;
import de.maxhenkel.voicechat.voice.client.Client;
import me.pieking1215.immersive_telephones.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.mixin.client.MixinClientAccessor;
import me.pieking1215.immersive_telephones.screen.TelephoneScreen;
import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.voice.client.TelephoneAudioChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;

public class ClientProxy implements IProxy {
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
}
