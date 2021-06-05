package me.pieking1215.immersive_telephones.common;

import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.common.network.SyncServerConfigPacket;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final Client CLIENT = new Client(BUILDER);

    // dedicated server & client in singleplayer
    public static final Server SERVER_SP_OR_DEDICATED = new Server(BUILDER);

    // when the client connects to a server, this field gets the server's values
    public static Server SERVER_MP = null;

    public static Server getActiveServerConfig(){
        return SERVER_MP != null ? SERVER_MP : SERVER_SP_OR_DEDICATED;
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // settings controlled by the client
    @SuppressWarnings("SameParameterValue")
    public static class Client {
        public final Atomic<Boolean> fancyCordRendering;
        public final Atomic<Boolean> accurateCordAttachment;
        public final Atomic<Boolean> handsetPose;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("Client");
            fancyCordRendering = Atomic.wrap(builder
                    .comment("Makes the handset cord curly, but uses many more polygons [false/true|default:true]")
                    .translation("fancyCordRendering.immersive_telephones.config")
                    .define("fancyCordRendering", true));
            accurateCordAttachment = Atomic.wrap(builder
                    .comment("Experimental method of connecting the phone cord to the handset in the player's hand [false/true|default:true]")
                    .translation("accurateCordAttachment.immersive_telephones.config")
                    .define("accurateCordAttachment", true));
            handsetPose = Atomic.wrap(builder
                    .comment("When holding a handset, players hold it to their head [false/true|default:true]")
                    .translation("handsetPose.immersive_telephones.config")
                    .define("handsetPose", true));
            builder.pop();
        }
    }

    public static class Atomic<T> {
        private final T defaultValue;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        private static Field defaultSupplier;

        static {
            try {
                defaultSupplier = ForgeConfigSpec.ConfigValue.class.getDeclaredField("defaultSupplier");
                defaultSupplier.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        private Atomic(T defaultValue, Supplier<T> getter, Consumer<T> setter){
            this.defaultValue = defaultValue;
            this.getter = getter;
            this.setter = setter;
        }

        public void set(T value){
            setter.accept(value);
        }

        public T get() {
            return getter.get();
        }

        public T getDefault(){
            return defaultValue;
        }

        public static <C> Atomic<C> wrap(ForgeConfigSpec.ConfigValue<C> cv) {
            Preconditions.checkState(defaultSupplier != null);

            try {
                //noinspection unchecked
                return new Atomic<>(((Supplier<C>) defaultSupplier.get(cv)).get(), cv::get, cv::set);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return new Atomic<>(null, () -> null, (v) -> {
            });
        }

        public static <C> Atomic<C> of(C value){
            AtomicReference<C> val = new AtomicReference<>(value);
            return new Atomic<>(value, val::get, val::set);
        }
    }

    // settings controlled by the server
    @SuppressWarnings("SameParameterValue")
    public static class Server {
        public final Atomic<Double> maxHandsetDistance;

        Server(){
            maxHandsetDistance = Atomic.of(10.0);
        }

        Server(ForgeConfigSpec.Builder builder) {
            builder.push("Server");

            maxHandsetDistance = Atomic.wrap(builder
                    .comment("Maximum distance from the base you can hold the handset before it gets pulled out of your hand [false/true|default:true]")
                    .translation("maxHandsetDistance.immersive_telephones.config")
                    .define("maxHandsetDistance", 10.0));

            builder.pop();
        }

        public SyncServerConfigPacket makePacket(){
            return new SyncServerConfigPacket(
                    maxHandsetDistance.get()
            );
        }

    }

    public static void makeDummyServerConfig(SyncServerConfigPacket pkt){
        SERVER_MP = new Server();
        SERVER_MP.maxHandsetDistance.set(pkt.maxHandsetDistance);
    }
}