package me.pieking1215.immersive_telephones.data.client;

import blusunrize.immersiveengineering.data.blockstates.ConnectorBlockBuilder;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.BlockRegister;
import me.pieking1215.immersive_telephones.common.block.peripheral.SpeakerBlock;
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
        telephoneTier1Block();
        switchboardT1Block();
        audioProviderRouterT1Block();
        audioReceiverRouterT1Block();
        speakerBlock();
    }

    private void telephoneTier1Block(){
        ResourceLocation particle = modLoc("blocks/telephone");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.TELEPHONE_T1.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .fixedModel(models().getExistingFile(modLoc("block/empty")))
                .layers(RenderType.getCutout())
                .autoRotationData()
                .build();
        disableUVLock(builder);
    }

    private void switchboardT1Block(){
        ResourceLocation particle = modLoc("blocks/switchboard_t1_side");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.SWITCHBOARD_T1.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .fixedModel(models().getExistingFile(modLoc("block/switchboard_t1")))
                .layers(RenderType.getSolid())
                .autoRotationData()
                .build();
    }

    private void audioProviderRouterT1Block(){
        ResourceLocation particle = modLoc("blocks/audio_provider_router_t1");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.AUDIO_PROVIDER_ROUTER_T1.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .fixedModel(models().getExistingFile(modLoc("block/audio_provider_router_t1")))
                .layers(RenderType.getSolid())
                .build();
    }

    private void audioReceiverRouterT1Block(){
        ResourceLocation particle = modLoc("blocks/audio_receiver_router_t1");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.AUDIO_RECEIVER_ROUTER_T1.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .fixedModel(models().getExistingFile(modLoc("block/audio_receiver_router_t1")))
                .layers(RenderType.getSolid())
                .build();
    }

    private void speakerBlock(){
        ResourceLocation particle = modLoc("blocks/speaker_off");
        VariantBlockStateBuilder builder = getVariantBuilder(BlockRegister.SPEAKER.get());
        ConnectorBlockBuilder.builder(this.models(), builder, (res, mod) -> res.texture("particle", particle))
                .binaryModel(SpeakerBlock.ACTIVE,
                        models().getExistingFile(modLoc("block/speaker_off")),
                        models().getExistingFile(modLoc("block/speaker_on")))
                .layers(RenderType.getSolid())
                .build();
    }

    private void disableUVLock(VariantBlockStateBuilder builder){
        try {
            Field f_models = ConfiguredModelList.class.getDeclaredField("models");
            f_models.setAccessible(true);

            Field f_uvLock = ConfiguredModel.class.getDeclaredField("uvLock");
            f_uvLock.setAccessible(true);

            builder.getModels().forEach((partialBlockstate, configuredModelList) -> {
                try {
                    //noinspection unchecked
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
