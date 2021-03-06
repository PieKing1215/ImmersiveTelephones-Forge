package me.pieking1215.immersive_telephones.common.block;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.peripheral.SpeakerBlock;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTier1Block;
import me.pieking1215.immersive_telephones.common.block.router.AudioProviderRouterTier1Block;
import me.pieking1215.immersive_telephones.common.block.router.AudioReceiverRouterTier1Block;
import me.pieking1215.immersive_telephones.common.block.switchboard.tier1.SwitchboardTier1Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegister {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveTelephone.MOD_ID);

    public static final RegistryObject<TelephoneTier1Block> TELEPHONE_T1 = BLOCKS.register("telephone_t1",
            () -> new TelephoneTier1Block(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<SwitchboardTier1Block> SWITCHBOARD_T1 = BLOCKS.register("switchboard_t1",
            () -> new SwitchboardTier1Block(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<AudioProviderRouterTier1Block> AUDIO_PROVIDER_ROUTER_T1 = BLOCKS.register("audio_provider_router_t1",
            () -> new AudioProviderRouterTier1Block(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<AudioReceiverRouterTier1Block> AUDIO_RECEIVER_ROUTER_T1 = BLOCKS.register("audio_receiver_router_t1",
            () -> new AudioReceiverRouterTier1Block(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<SpeakerBlock> SPEAKER = BLOCKS.register("speaker",
            () -> new SpeakerBlock(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));


}
