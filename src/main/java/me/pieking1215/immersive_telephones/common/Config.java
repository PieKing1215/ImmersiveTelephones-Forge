package me.pieking1215.immersive_telephones.common;

import net.minecraftforge.common.ForgeConfigSpec;

@SuppressWarnings("unused")
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Client CLIENT = new Client(BUILDER);
    public static final Server SERVER = new Server(BUILDER);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // settings controlled by the client
    @SuppressWarnings("SameParameterValue")
    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> fancyCordRendering;
        public final ForgeConfigSpec.ConfigValue<Boolean> accurateCordAttachment;
        public final ForgeConfigSpec.ConfigValue<Boolean> handsetPose;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("Client");
            fancyCordRendering = builder
                    .comment("Makes the handset cord curly, but uses many more polygons [false/true|default:true]")
                    .translation("fancyCordRendering.immersive_telephones.config")
                    .define("fancyCordRendering", true);
            accurateCordAttachment = builder
                    .comment("Experimental method of connecting the phone cord to the handset in the player's hand [false/true|default:true]")
                    .translation("accurateCordAttachment.immersive_telephones.config")
                    .define("accurateCordAttachment", true);
            handsetPose = builder
                    .comment("When holding a handset, players hold it to their head [false/true|default:true]")
                    .translation("handsetPose.immersive_telephones.config")
                    .define("handsetPose", true);
            builder.pop();
        }
    }

    // settings controlled by the server
    @SuppressWarnings("SameParameterValue")
    public static class Server {

        Server(ForgeConfigSpec.Builder builder) {
            builder.push("Server");
            builder.pop();
        }
    }
}