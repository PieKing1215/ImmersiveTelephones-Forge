package me.pieking1215.immersive_telephones;

import net.minecraftforge.common.ForgeConfigSpec;

// TODO
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
//        public final ForgeConfigSpec.ConfigValue<Boolean> enabled;
//        public static final double ANIM_MIN = 0.1;
//        public static final double ANIM_MAX = 2.5;
//        public final ForgeConfigSpec.ConfigValue<Double> animationSpeed;
//        public final ForgeConfigSpec.ConfigValue<Boolean> disableAnimation;

        public General(ForgeConfigSpec.Builder builder) {
//            builder.push("General");
//            enabled = builder
//                    .comment("Enables/Disables the whole Mod [false/true|default:true]")
//                    .translation("enable.immersive_telephones.config")
//                    .define("enableMod", true);
//            animationSpeed = builder
//                    .comment("Modifier for the sneak animation speed [false/true|default:true]")
//                    .translation("speed.immersive_telephones.config")
//                    .defineInRange("animationSpeed", 1.0, ANIM_MIN, ANIM_MAX);
//            disableAnimation = builder
//                    .comment("Disables the smooth sneak animation [false/true|default:false]")
//                    .translation("disableanimation.immersive_telephones.config")
//                    .define("disableAnimation", false);
//            builder.pop();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean getBoolSafe(ForgeConfigSpec.ConfigValue<Boolean> bool, boolean defaultValue){
        Object o = bool.get();
        if(!(o instanceof Boolean)){
            //java thinks this if will never be true but we know it can be
            if(o instanceof String){
                String st = (String)o;

                // an invalid string (eg. "foo") should revert to the default
                if(defaultValue){
                    bool.set(!st.equalsIgnoreCase("false"));
                }else{
                    bool.set(st.equalsIgnoreCase("true"));
                }
            }else{
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }

    @SuppressWarnings("ConstantConditions")
    public static double getDoubleSafe(ForgeConfigSpec.ConfigValue<Double> bool, double defaultValue){
        Object o = bool.get();
        if(!(o instanceof Double)){
            //java thinks this if will never be true but we know it can be
            if(o instanceof Float){
                bool.set((Double)o);
            }else if(o instanceof String){
                String st = (String)o;

                // an invalid string (eg. "foo") should revert to the default
                try{
                    double d = Double.parseDouble(st);
                    bool.set(d);
                }catch(Exception e){
                    e.printStackTrace();
                    bool.set(defaultValue);
                }
            }else{
                bool.set(defaultValue);
            }
        }
        return bool.get();
    }
}