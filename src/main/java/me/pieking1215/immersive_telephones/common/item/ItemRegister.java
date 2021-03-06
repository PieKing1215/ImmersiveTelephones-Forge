package me.pieking1215.immersive_telephones.common.item;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.BlockRegister;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class ItemRegister {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveTelephone.MOD_ID);

    public static final RegistryObject<BlockItem> TELEPHONE_BLOCK = ITEMS.register("telephone_t1",
            () -> new BlockItem(BlockRegister.TELEPHONE_T1.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<BlockItem> SWITCHBOARD_T1 = ITEMS.register("switchboard_t1",
            () -> new BlockItem(BlockRegister.SWITCHBOARD_T1.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<BlockItem> AUDIO_PROVIDER_ROUTER_T1 = ITEMS.register("audio_provider_router_t1",
            () -> new BlockItem(BlockRegister.AUDIO_PROVIDER_ROUTER_T1.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<BlockItem> AUDIO_RECEIER_ROUTER_T1 = ITEMS.register("audio_receiver_router_t1",
            () -> new BlockItem(BlockRegister.AUDIO_RECEIVER_ROUTER_T1.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<BlockItem> SPEAKER = ITEMS.register("speaker",
            () -> new BlockItem(BlockRegister.SPEAKER.get(), new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final RegistryObject<Item> TELEPHONE_HANDSET = ITEMS.register("handset",
            () -> new HandsetItem(new Item.Properties()
                    .group(ItemGroup.REDSTONE)
                    .maxStackSize(1)
            ));

}
