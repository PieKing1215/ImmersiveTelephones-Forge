package me.pieking1215.immersive_telephones.net;

import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StartCallPacket {

    private BlockPos caller;
    private String callee;

    public StartCallPacket(BlockPos caller, String callee) {
        this.caller = caller;
        this.callee = callee;
    }

    void encode(PacketBuffer buf){
        buf.writeBlockPos(caller);
        buf.writeString(callee);
    }

    static StartCallPacket decode(PacketBuffer buf){
        return new StartCallPacket(buf.readBlockPos(), buf.readString(16)); // TODO: be defensive
    }


    void handle(Supplier<NetworkEvent.Context> ctx){

        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayerEntity player = ctx.get().getSender();

                // TODO: these are only rough sanity checks
                if(player != null && player.world.isBlockLoaded(caller) && caller.distanceSq(player.getPosition()) < 16 * 16){
                    TileEntity tileEntity = player.world.getTileEntity(caller);
                    if(tileEntity instanceof TelephoneTileEntity){
                        TelephoneTileEntity te = (TelephoneTileEntity)tileEntity;

                        te.findConnectedPhones().stream().filter(t -> t.getName().equals(callee)).findFirst().ifPresent(other -> {
                            other.beingCalled(te);
                        });
                    }
                }
            }

        });

        ctx.get().setPacketHandled(true);
    }

}
