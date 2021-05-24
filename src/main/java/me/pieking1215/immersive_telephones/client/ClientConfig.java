package me.pieking1215.immersive_telephones.client;

import me.pieking1215.immersive_telephones.common.Config;
import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.forge.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.forge.gui.entries.FloatListEntry;
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

            new CategoryHandler(builder, "server", Config.getActiveServerConfig() == Config.SERVER_MP){{
                addDouble("maxHandsetDistance", Config.getActiveServerConfig().maxHandsetDistance);
            }};

            return builder.setSavingRunnable(Config.SPEC::save).build();
        });
    }

    private static class CategoryHandler {

        final ConfigEntryBuilder builder;
        final ConfigCategory category;
        final boolean readOnly;

        public CategoryHandler(ConfigBuilder configBuilder, String category) {
            this(configBuilder, category, false);
        }

        public CategoryHandler(ConfigBuilder configBuilder, String category, boolean readOnly) {
            this.builder = configBuilder.entryBuilder();
            this.category = configBuilder.getOrCreateCategory(new TranslationTextComponent("config.immersive_telephones.category." + category));
            this.readOnly = readOnly;
        }

        void addBoolean(String key, Config.Atomic<Boolean> bool) {
            addBoolean(key, bool, this.readOnly);
        }

        void addBoolean(String key, Config.Atomic<Boolean> bool, boolean readOnly){
            BooleanListEntry e = builder.startBooleanToggle(
                    new TranslationTextComponent("config.immersive_telephones." + key)
                    , bool.get())
                    .setSaveConsumer(readOnly ? v -> {} : bool::set)
                    .setDefaultValue(readOnly ? bool.get() : bool.getDefault())
                    .setTooltip(Arrays.stream(I18n.format("tooltip.config.immersive_telephones." + key).split("\n")).map(StringTextComponent::new).toArray(StringTextComponent[]::new))
                    .build();
            e.setEditable(!readOnly);
            category.addEntry(e);
        }

        void addDouble(String key, Config.Atomic<Double> doubleValue){
            addDouble(key, doubleValue, this.readOnly);
        }

        void addDouble(String key, Config.Atomic<Double> doubleValue, boolean readOnly){
            // ah yes great formatting
            DoubleListEntry e = builder.startDoubleField(
                    new TranslationTextComponent("config.immersive_telephones." + key)
                    , doubleValue.get())

                    .setSaveConsumer(readOnly ? v -> {} : doubleValue::set)
                    .setDefaultValue(readOnly ? doubleValue.get() : doubleValue.getDefault()) // TODO: reflect into ForgeConfigSpec.ConfigValue or something to get the actual default
                    .setTooltip(Arrays.stream(I18n.format("tooltip.config.immersive_telephones." + key).split("\n")).map(StringTextComponent::new).toArray(StringTextComponent[]::new))
                    .build();
            e.setEditable(!readOnly);
            category.addEntry(e);
        }
    }



}
