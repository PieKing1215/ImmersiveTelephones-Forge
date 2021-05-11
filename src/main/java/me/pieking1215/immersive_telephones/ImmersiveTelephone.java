package me.pieking1215.immersive_telephones;

import me.pieking1215.immersive_telephones.block.BlockRegister;
import me.pieking1215.immersive_telephones.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.client.ClientConfig;
import me.pieking1215.immersive_telephones.command.ProbeConnectionCommand;
import me.pieking1215.immersive_telephones.entity.EntityRegister;
import me.pieking1215.immersive_telephones.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.item.HandsetItem;
import me.pieking1215.immersive_telephones.item.ItemRegister;
import me.pieking1215.immersive_telephones.net.ImmersiveTelephonePacketHandler;
import me.pieking1215.immersive_telephones.proxy.ClientProxy;
import me.pieking1215.immersive_telephones.proxy.IProxy;
import me.pieking1215.immersive_telephones.proxy.ServerProxy;
import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.tile_entity.TileEntityRegister;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Mod("immersive_telephones")
public class ImmersiveTelephone {
    public static final String MOD_ID = "immersive_telephones";

    private static final Logger LOGGER = LogManager.getLogger();

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ImmersiveTelephone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);

        MinecraftForge.EVENT_BUS.register(this);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegister.ITEMS.register(bus);
        BlockRegister.BLOCKS.register(bus);
        TileEntityRegister.TILE_ENTITIES.register(bus);
        EntityRegister.ENTITY_TYPES.register(bus);

        ImmersiveTelephonePacketHandler.init();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ClientConfig.registerClothConfig();
        });
    }

    public static boolean useGlobalVoice(PlayerEntity local, PlayerEntity other){
        return true;
    }

    public static List<PlayerEntity> getGlobalVoiceConnected(PlayerEntity local){
        List<PlayerEntity> allOtherPlayers = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream().map(e -> (PlayerEntity)e).filter(p -> !local.getUniqueID().equals(p.getUniqueID())).collect(Collectors.toList());
        return allOtherPlayers;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ProbeConnectionCommand.register(event.getDispatcher());
    }

    HashMap<String, Integer> m = new HashMap<>();

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
//                ev.setResult(Event.Result.DENY);
//                ev.getEntityItem().remove();
                return;
            }

            if(!ev.getEntityItem().world.isBlockLoaded(telPos)){
                // the position this is supposed to be connected to is not chunk loaded
//                ev.setResult(Event.Result.DENY);
//                ev.getEntityItem().remove();
                return;
            }

            TileEntity te = ev.getEntityItem().world.getTileEntity(telPos);
            if(!(te instanceof TelephoneTileEntity)){
                // there is no telephone here
//                ev.setResult(Event.Result.DENY);
//                ev.getEntityItem().remove();
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
