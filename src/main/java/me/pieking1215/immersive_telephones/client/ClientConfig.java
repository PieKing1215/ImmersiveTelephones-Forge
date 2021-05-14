package me.pieking1215.immersive_telephones.client;

import me.pieking1215.immersive_telephones.common.Config;
import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Arrays;

class ClientConfig {

    static void registerClothConfig() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> {
            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslationTextComponent("config.immersive_telephones.title"));
            builder.setDefaultBackgroundTexture(new ResourceLocation("minecraft:textures/block/spruce_planks.png"));
            builder.transparentBackground();

            new CategoryHandler(builder, "client"){{
                addBoolean("fancyCordRendering", Config.CLIENT.fancyCordRendering);
                addBoolean("accurateCordAttachment", Config.CLIENT.accurateCordAttachment);
                addBoolean("handsetPose", Config.CLIENT.handsetPose);
            }};

            //noinspection EmptyClassInitializer
            new CategoryHandler(builder, "server"){{
                // none yet
            }};

            return builder.setSavingRunnable(Config.SPEC::save).build();
        });
    }

    private static class CategoryHandler {

        final ConfigEntryBuilder builder;
        final ConfigCategory category;

        public CategoryHandler(ConfigBuilder configBuilder, String category) {
            this.builder = configBuilder.entryBuilder();
            this.category = configBuilder.getOrCreateCategory(new TranslationTextComponent("config.immersive_telephones.category." + category));
        }

        void addBoolean(String key, ForgeConfigSpec.ConfigValue<Boolean> bool){
            category.addEntry(
                    builder.startBooleanToggle(
                            new TranslationTextComponent("config.immersive_telephones." + key)
                            , bool.get())
                            .setSaveConsumer(bool::set)
                            .setDefaultValue(true)
                            .setTooltip(Arrays.stream(I18n.format("tooltip.config.immersive_telephones." + key).split("\n")).map(StringTextComponent::new).toArray(StringTextComponent[]::new))
                            .build());
        }
    }



}
