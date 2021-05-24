package me.pieking1215.immersive_telephones.common.block;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
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


}
