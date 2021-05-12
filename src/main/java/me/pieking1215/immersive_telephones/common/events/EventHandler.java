package me.pieking1215.immersive_telephones.common.events;

import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void onItemPickedUp(EntityItemPickupEvent ev){
        if(ev.getItem().world.isRemote) return;

        // server side

        ItemStack stack = ev.getItem().getItem();
        if(stack.getItem() instanceof HandsetItem){
            BlockPos telPos = HandsetItem.getConnectedPosition(stack);

            if(telPos == null) {
                // invalid item
                ev.setResult(Event.Result.DENY);
                ev.getItem().remove();
                return;
            }

            //noinspection deprecation
            if(!ev.getItem().world.isBlockLoaded(telPos)){
                // the position this is supposed to be connected to is not chunk loaded
                ev.setResult(Event.Result.DENY);
                ev.getItem().remove();
                return;
            }

            TileEntity te = ev.getItem().world.getTileEntity(telPos);
            if(!(te instanceof TelephoneTileEntity)){
                // there is no telephone here
                ev.setResult(Event.Result.DENY);
                ev.getItem().remove();
                return;
            }

            TelephoneTileEntity tel = (TelephoneTileEntity) te;
            tel.reconnectHandset((ServerPlayerEntity) ev.getPlayer());

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

            BlockPos telPos = HandsetItem.getConnectedPosition(stack);

            if(telPos == null) {
                // invalid item
                return;
            }

            //noinspection deprecation
            if(!ev.getEntityItem().world.isBlockLoaded(telPos)){
                // the position this is supposed to be connected to is not chunk loaded
                return;
            }

            TileEntity te = ev.getEntityItem().world.getTileEntity(telPos);
            if(!(te instanceof TelephoneTileEntity)){
                // there is no telephone here
                return;
            }

            TelephoneTileEntity tel = (TelephoneTileEntity) te;
            tel.disconnectHandset(ent);

        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock ev){
        if(ev.getItemStack().getItem() instanceof HandsetItem && ev.getWorld().getBlockState(ev.getPos()).getBlock() instanceof TelephoneBlock){
            ev.setUseBlock(Event.Result.ALLOW);
            ev.setUseItem(Event.Result.DENY);
        }
    }
}
