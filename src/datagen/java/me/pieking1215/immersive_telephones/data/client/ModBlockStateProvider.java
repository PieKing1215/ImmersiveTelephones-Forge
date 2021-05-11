package me.pieking1215.immersive_telephones.data.client;

import blusunrize.immersiveengineering.data.blockstates.ConnectorBlockBuilder;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.block.BlockRegister;
import me.pieking1215.immersive_telephones.block.TelephoneBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.lang.reflect.Field;
import java.util.List;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ImmersiveTelephone.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        telephoneBlock();
    }

    private void telephoneBlock(){
        ResourceLocation particle = modLoc("blocks/telephone");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.TELEPHONE_BLOCK.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .binaryModel(TelephoneBlock.HANDSET,
                        models().getExistingFile(modLoc("block/telephone_base")),
                        models().getExistingFile(modLoc("block/telephone_with_handset")))
                .layers(RenderType.getCutout())
                .autoRotationData()
                .build();
        disableUVLock(builder);
    }

    private void disableUVLock(VariantBlockStateBuilder builder){
        try {
            Field f_models = ConfiguredModelList.class.getDeclaredField("models");
            f_models.setAccessible(true);

            Field f_uvLock = ConfiguredModel.class.getDeclaredField("uvLock");
            f_uvLock.setAccessible(true);

            builder.getModels().forEach((partialBlockstate, configuredModelList) -> {
                try {
                    ((List<ConfiguredModel>) f_models.get(configuredModelList)).forEach(m -> {
                        try {
                            f_uvLock.set(m, false);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
