package me.pieking1215.immersive_telephones.tile_entity;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.block.BlockRegister;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegister {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ImmersiveTelephone.MOD_ID);

    public static final RegistryObject<TileEntityType<TelephoneTileEntity>> TELEPHONE = TILE_ENTITIES.register("telephone",
            () -> TileEntityType.Builder.create(TelephoneTileEntity::new, BlockRegister.TELEPHONE_BLOCK.get()).build(null));

}
