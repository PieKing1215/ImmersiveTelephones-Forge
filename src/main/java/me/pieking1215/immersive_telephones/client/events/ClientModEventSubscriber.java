package me.pieking1215.immersive_telephones.client.events;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.BlockRegister;
import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.EntityRegister;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntityRenderer;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.item.ItemRegister;
import me.pieking1215.immersive_telephones.client.tile_entity.TelephoneTileEntityRenderer;
import me.pieking1215.immersive_telephones.common.tile_entity.TileEntityRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ImmersiveTelephone.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent ev){
        ClientRegistry.bindTileEntityRenderer(TileEntityRegister.TELEPHONE.get(), TelephoneTileEntityRenderer::new);

        RenderTypeLookup.setRenderLayer(BlockRegister.TELEPHONE_BLOCK.get(),
                rt -> rt == RenderType.getSolid() || rt == RenderType.getCutout());

        RenderingRegistry.registerEntityRenderingHandler(EntityRegister.HANDSET.get(), (EntityRendererManager renderManagerIn) -> new HandsetEntityRenderer(renderManagerIn, Minecraft.getInstance().getItemRenderer()));
    }

    @SubscribeEvent
    public static void onRegisterItemColors(ColorHandlerEvent.Item event){
        event.getItemColors().register(HandsetItem::getItemColor, ItemRegister.TELEPHONE_HANDSET.get());
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(ColorHandlerEvent.Block event){
        event.getBlockColors().register(TelephoneBlock::getColor, BlockRegister.TELEPHONE_BLOCK.get());
    }

}
