package me.pieking1215.immersive_telephones.client;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ImmersiveTelephone.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {

    @SubscribeEvent
    public void onClick(InputEvent.ClickInputEvent ev){
        if(ImmersiveTelephone.proxy.shouldCancelClick(ev)) {
            ev.setResult(Event.Result.DENY);
            ev.setCanceled(true);
        }
    }
}
