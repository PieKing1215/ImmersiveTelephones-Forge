package me.pieking1215.immersive_telephones.client;

public class ClientConfig {

    public static void registerClothConfig() {
        // TODO
//        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, parent) -> {
//            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslationTextComponent("config.immersive_telephones.title"));
//            builder.setDefaultBackgroundTexture(new ResourceLocation("minecraft:textures/block/spruce_planks.png"));
//            builder.transparentBackground();
//
//            ConfigEntryBuilder eb = builder.getEntryBuilder();
//            ConfigCategory general = builder.getOrCreateCategory(new TranslationTextComponent("key.immersive_telephones.category.general"));
//            general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.immersive_telephones.enable"), Config.getBoolSafe(Config.GENERAL.enabled, true)).setDefaultValue(true).setSaveConsumer(Config.GENERAL.enabled::set)/*.setTooltip(Arrays.asList(I18n.format("tooltip.config.immersive_telephones.enable").split("\n")).stream().map(StringTextComponent::new).toArray(StringTextComponent[]::new))*/.build());
//            //general.addEntry(eb.startDoubleField("config.immersive_telephones.animationSpeed", getDoubleSafe(GENERAL.animationSpeed, 1.0)).setDefaultValue(1.0).setMin(0.1).setMax(2.5).setSaveConsumer(GENERAL.animationSpeed::set).setTooltip(I18n.format("tooltip.config.immersive_telephones.animationSpeed").split("\n")).build());
//
//            int nTicks = (int) ((Config.General.ANIM_MAX - Config.General.ANIM_MIN) / 0.1) + 1;
//
//            // map [ANIM_MIN, ANIM_MAX] to [0, nTicks]
//            int animV = (int) (((Config.getDoubleSafe(Config.GENERAL.animationSpeed, 1.0) - Config.General.ANIM_MIN) / (Config.General.ANIM_MAX - Config.General.ANIM_MIN)) * nTicks);
//            int animDef = (int) (((1.0 - Config.General.ANIM_MIN) / (Config.General.ANIM_MAX - Config.General.ANIM_MIN)) * nTicks);
//
//            general.addEntry(eb.startIntSlider(new TranslationTextComponent("config.immersive_telephones.animationSpeed"), animV, 0, nTicks).setDefaultValue(animDef).setSaveConsumer((i) -> {
//                // map [0, nTicks] to [ANIM_MIN, ANIM_MAX]
//                double thru = i / (double)nTicks;
//                double v = Config.General.ANIM_MIN + (thru * (Config.General.ANIM_MAX - Config.General.ANIM_MIN));
//                v = Math.round(v * 20.0) / 20.0;
//                Config.GENERAL.animationSpeed.set(v);
//            }).setTextGetter((i) -> {
//                // map [0, nTicks] to [ANIM_MIN * 100, ANIM_MAX * 100]
//                double thru = i / (double)nTicks;
//                double v = Config.General.ANIM_MIN + (thru * (Config.General.ANIM_MAX - Config.General.ANIM_MIN));
//                v = Math.round(v * 20.0) / 20.0;
//                int percent = (int) (100 * v);
//
//                percent = (int) (Math.round(percent/10.0) * 10);
//                return new StringTextComponent(percent + "%");
//
//            })/*.setTooltip(Arrays.asList(I18n.format("tooltip.config.immersive_telephones.animationSpeed").split("\n")).stream().map(StringTextComponent::new).toArray(StringTextComponent[]::new))*/.build());
//
//            general.addEntry(eb.startBooleanToggle(new TranslationTextComponent("config.immersive_telephones.disableAnimation"), Config.getBoolSafe(Config.GENERAL.disableAnimation, false)).setDefaultValue(false).setSaveConsumer(Config.GENERAL.disableAnimation::set)/*.setTooltip(Arrays.asList(I18n.format("tooltip.config.immersive_telephones.disableAnimation").split("\n")).stream().map(StringTextComponent::new).toArray(StringTextComponent[]::new))*/.build());
//
//            return builder.setSavingRunnable(Config.spec::save).build();
//        });
    }

}
