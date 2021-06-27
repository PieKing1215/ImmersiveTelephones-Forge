package me.pieking1215.immersive_telephones.common.block;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.BlockRegister;
import me.pieking1215.immersive_telephones.common.block.peripheral.SpeakerTileEntity;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.common.block.switchboard.tier1.SwitchboardTier1TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegister {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ImmersiveTelephone.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<TelephoneTileEntity>> TELEPHONE = TILE_ENTITIES.register("telephone",
            () -> TileEntityType.Builder.create(TelephoneTileEntity::new, BlockRegister.TELEPHONE_BLOCK.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<SwitchboardTier1TileEntity>> SWITCHBOARD_T1 = TILE_ENTITIES.register("switchboard_t1",
            () -> TileEntityType.Builder.create(SwitchboardTier1TileEntity::new, BlockRegister.SWITCHBOARD_T1.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<SpeakerTileEntity>> SPEAKER = TILE_ENTITIES.register("speaker",
            () -> TileEntityType.Builder.create(SpeakerTileEntity::new, BlockRegister.SPEAKER.get()).build(null));
}
