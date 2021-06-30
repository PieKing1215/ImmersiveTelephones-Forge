package me.pieking1215.immersive_telephones.common.block.peripheral;

import com.google.common.base.Preconditions;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.IAudioPlayerHandler;
import me.pieking1215.immersive_telephones.common.block.IAudioProvider;
import me.pieking1215.immersive_telephones.common.block.IAudioReceiver;
import me.pieking1215.immersive_telephones.common.block.ICallable;
import me.pieking1215.immersive_telephones.common.block.TileEntityRegister;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SpeakerTileEntity extends TileEntity implements ICallable, IAudioReceiver, IAudioPlayerHandler, ITickableTileEntity {

    private UUID uuid;
    private String number = "000";

    protected final List<ICallable> inCallWith = new ArrayList<>();
    protected boolean inCall = false;

    public SpeakerTileEntity() {
        super(TileEntityRegister.SPEAKER.get());
        uuid = UUID.randomUUID();
    }

    //region <networking>

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putUniqueId("tel_UUID", uuid);
        nbt.putString("name", number);

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        number = nbt.getString("name");
        if(nbt.contains("tel_UUID")) uuid = nbt.getUniqueId("tel_UUID");

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putUniqueId("tel_UUID", uuid);
        nbt.putString("name", number);

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        uuid = nbt.getUniqueId("tel_UUID");
        number = nbt.getString("name");

        Preconditions.checkNotNull(world);

        if(world.isRemote){
            // client side
            ImmersiveTelephone.proxy.registerTelephoneAudioChannel(this);
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        handleUpdateTag(getBlockState(), pkt.getNbtCompound());
    }

    //endregion

    @Override
    public void onDialed(ICallable dialedBy, String query) {
        Preconditions.checkNotNull(world);

        dialedBy.onAnsweredCall(this);

        inCall = true;
        inCallWith.add(dialedBy);

        inCallWith.forEach(o -> {
            dialedBy.onAddedToCall(this, o);
            o.onAddedToCall(this, dialedBy);

            inCallWith.forEach(o2 -> {
                o.onAddedToCall(this, o2);
                o2.onAddedToCall(this, o);
            });

        });

        world.setBlockState(pos, this.getBlockState().with(SpeakerBlock.ACTIVE, true));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

    }

    @Override
    public boolean isStillCalling(ICallable other) {
        // speakers cannot call anyone
        return false;
    }

    @Override
    public void onAnsweredCall(ICallable answerer) {
        // speakers cannot call anyone
    }

    @Override
    public void onAddedToCall(ICallable whoAdded, ICallable added) {
        Preconditions.checkNotNull(world);

        if(whoAdded != this && inCallWith.contains(whoAdded) && added instanceof IAudioProvider){
            if(added == this || inCallWith.contains(added)){
                // bonus check because I definitely got the logic in answerPhone wrong
                return;
            }

            inCall = true;
            inCallWith.add(added);

            world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    public void onLeftCall(ICallable leaver) {
        Preconditions.checkNotNull(world);

        inCallWith.remove(leaver);
        if(inCallWith.isEmpty()){
            inCall = false;

            world.setBlockState(pos, this.getBlockState().with(SpeakerBlock.ACTIVE, false));
            world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    public void setNumber(String num){
        number = num;
    }

    @Override
    public String getID() {
        return number;
    }

    @Override
    public UUID getReceiverUUID() {
        return uuid;
    }

    @Override
    public Vector3d getReceiverPos() {
        return Vector3d.copyCentered(getPos());
    }

    @Override
    public void recieveAudio(MicPacket packet) {
        Preconditions.checkNotNull(world);
        Preconditions.checkState(!world.isRemote);

        // server

        Preconditions.checkNotNull(Main.SERVER_VOICE_EVENTS.getServer());

        Server voiceServer = Main.SERVER_VOICE_EVENTS.getServer();

        // gather all players within 32 blocks of a phone that is in a call with this phone
        float distance = 32;

        NetworkMessage msg = new NetworkMessage(new SoundPacket(getReceiverUUID(), packet.getData(), packet.getSequenceNumber()));
        world.getEntitiesWithinAABB(PlayerEntity.class,
                AxisAlignedBB.fromVector(getReceiverPos()).grow(distance)
        ).stream()
                .map(pl -> voiceServer.getConnections().get(pl.getUniqueID()))
                .filter(Objects::nonNull)
                .forEach(c -> {
                    try {
                        c.send(voiceServer, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public Vector3d getPlaybackPosition() {
        return Vector3d.copyCentered(getPos());
    }

    @Override
    public boolean shouldBeMono() {
        return false;
    }

    @Override
    public UUID getChannelUUID() {
        return uuid;
    }

    @Override
    public void tick() {
        Preconditions.checkNotNull(world);

        // this time check could be staggered to offset lag but I don't think it'll really be a problem
        if(!world.isRemote && world.getGameTime() % 20 == 0 && getBlockState().get(SpeakerBlock.ACTIVE) != inCall){
            world.setBlockState(pos, this.getBlockState().with(SpeakerBlock.ACTIVE, inCall));
            world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }
}
