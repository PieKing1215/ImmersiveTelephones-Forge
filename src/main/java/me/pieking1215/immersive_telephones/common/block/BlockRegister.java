package me.pieking1215.immersive_telephones.common.block;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.router.AudioRouterTier1Block;
import me.pieking1215.immersive_telephones.common.block.switchboard.SwitchboardTier1Block;
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

    public static final RegistryObject<TelephoneBlock> TELEPHONE_BLOCK = BLOCKS.register("telephone",
            () -> new TelephoneBlock(AbstractBlock.Properties.create(Material.PISTON)
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

    public static final RegistryObject<AudioRouterTier1Block> AUDIO_ROUTER_T1 = BLOCKS.register("audio_router_t1",
            () -> new AudioRouterTier1Block(AbstractBlock.Properties.create(Material.PISTON)
                    .hardnessAndResistance(1f, 30f)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(-1)
                    .sound(SoundType.METAL)));


}
