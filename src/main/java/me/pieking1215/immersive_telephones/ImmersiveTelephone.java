package me.pieking1215.immersive_telephones;

import me.pieking1215.immersive_telephones.common.Config;
import me.pieking1215.immersive_telephones.common.block.BlockRegister;
import me.pieking1215.immersive_telephones.common.command.ProbeConnectionCommand;
import me.pieking1215.immersive_telephones.common.entity.EntityRegister;
import me.pieking1215.immersive_telephones.common.events.EventHandler;
import me.pieking1215.immersive_telephones.common.item.ItemRegister;
import me.pieking1215.immersive_telephones.common.network.ImmersiveTelephonePacketHandler;
import me.pieking1215.immersive_telephones.client.ClientProxy;
import me.pieking1215.immersive_telephones.common.IProxy;
import me.pieking1215.immersive_telephones.common.ServerProxy;
import me.pieking1215.immersive_telephones.common.tile_entity.TileEntityRegister;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("immersive_telephones")
public class ImmersiveTelephone {
    public static final String MOD_ID = "immersive_telephones";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ImmersiveTelephone() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegister.ITEMS.register(bus);
        BlockRegister.BLOCKS.register(bus);
        TileEntityRegister.TILE_ENTITIES.register(bus);
        EntityRegister.ENTITY_TYPES.register(bus);

        ImmersiveTelephonePacketHandler.init();

        proxy.loadConfig();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ProbeConnectionCommand.register(event.getDispatcher());
    }
}
