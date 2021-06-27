package me.pieking1215.immersive_telephones.common.events;

import me.pieking1215.immersive_telephones.common.Config;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.network.ImmersiveTelephonePacketHandler;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTileEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class EventHandler {

    @SubscribeEvent
    public void onItemPickedUp(EntityItemPickupEvent ev){
        if(ev.getItem().world.isRemote) return;

        // server side

        ItemStack stack = ev.getItem().getItem();
        if(stack.getItem() instanceof HandsetItem){

            Optional<TelephoneTileEntity> o_tel = HandsetItem.findConnectedTE(stack, ev.getItem().world);

            if(o_tel.isPresent()){
                o_tel.get().reconnectHandset((ServerPlayerEntity) ev.getPlayer());
            }else{
                ev.setResult(Event.Result.DENY);
                ev.getItem().remove();
            }
        }
    }

    @SubscribeEvent
    public void onItemDropped(ItemTossEvent ev){
        if(ev.getEntityItem().world.isRemote) return;

        // server side

        ItemStack stack = ev.getEntityItem().getItem();
        if(stack.getItem() instanceof HandsetItem){
            ItemEntity itemEntity = ev.getEntityItem();
            ev.setCanceled(true);
            HandsetEntity ent = new HandsetEntity(itemEntity.getEntityWorld(), itemEntity.getPosX(), itemEntity.getPosY(), itemEntity.getPosZ(), stack);
            ent.setMotion(itemEntity.getMotion());
            ent.getEntityWorld().addEntity(ent);

            HandsetItem.findConnectedTE(stack, ev.getEntityItem().world).ifPresent(tel -> tel.disconnectHandset(ent));
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock ev){
        if(ev.getItemStack().getItem() instanceof HandsetItem && ev.getWorld().getBlockState(ev.getPos()).getBlock() instanceof TelephoneBlock){
            ev.setUseBlock(Event.Result.ALLOW);
            ev.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onClientConnected(PlayerEvent.PlayerLoggedInEvent ev){
        if(FMLEnvironment.dist == Dist.DEDICATED_SERVER){
            // send server config to client
            ImmersiveTelephonePacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)ev.getPlayer()),
                    Config.SERVER_SP_OR_DEDICATED.makePacket());
        }
    }

}
