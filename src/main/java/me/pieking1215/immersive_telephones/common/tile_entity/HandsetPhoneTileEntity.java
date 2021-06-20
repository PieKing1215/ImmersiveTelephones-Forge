package me.pieking1215.immersive_telephones.common.tile_entity;

import com.google.common.base.Preconditions;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.Config;
import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.item.ItemRegister;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class HandsetPhoneTileEntity extends BasePhoneTileEntity {

    private Entity handsetEntity = null;
    private int missingHandsetEntityID = -1;

    // TODO: this whole concept is insanely stupid on so many levels
    @OnlyIn(Dist.CLIENT)
    public Object clientHandItemMatrix4f;
    @OnlyIn(Dist.CLIENT)
    public Object clientCameraMatrix4f;

    protected float cordLength;

    public HandsetPhoneTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        cordLength = 6;
    }

    @Override
    public void tick() {
        super.tick();
        Preconditions.checkNotNull(world);

        if(!world.isRemote) {
            // server
            ServerWorld sw = (ServerWorld) this.world;

            if(world.getGameTime() % 10 == 0 && !getBlockState().get(TelephoneBlock.HANDSET) && handsetEntity == null){
                // the handset entity got lost

                sw.getLoadedEntitiesWithinAABB(HandsetEntity.class, AxisAlignedBB.fromVector(Vector3d.copyCentered(getPos())).grow(20)).stream()
                        .filter(e -> isTheHandset(e.getItem()))
                        .findFirst().ifPresent(this::disconnectHandset);

                sw.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, AxisAlignedBB.fromVector(Vector3d.copyCentered(getPos())).grow(20)).stream()
                        .filter(e -> isTheHandset(e.getHeldItemMainhand())
                                || isTheHandset(e.getHeldItemOffhand()))
                        .findFirst().ifPresent(this::reconnectHandset);
            }

            if(handsetEntity != null) {

                double cordMaxDist = Config.getActiveServerConfig().maxHandsetDistance.get();

                if(handsetEntity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity pl = (ServerPlayerEntity) handsetEntity;

                    boolean dropIfPresent = this.getPos().distanceSq(pl.getPositionVec(), true) > cordMaxDist * cordMaxDist;

                    //boolean found = false;

                    for (int i = 0; i < pl.inventory.getSizeInventory(); i++) {
                        ItemStack stack = pl.inventory.getStackInSlot(i);
                        if (isTheHandset(stack)) {
                            //found = true;

                            if (dropIfPresent || (pl.getHeldItemMainhand() != stack && pl.getHeldItemOffhand() != stack) || (pl.openContainer != null && pl.openContainer != pl.container)) {
                                pl.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

                                HandsetEntity item = new HandsetEntity(this.world, pl.getPosX(), pl.getPosYEye(), pl.getPosZ(), stack);
                                Vector3d vel = new Vector3d(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()).subtract(item.getPositionVec()).scale(0.04);
                                item.setMotion(vel.x, vel.y, vel.z);
                                item.setPickupDelay(40);
                                item.setThrowerId(pl.getUniqueID());
                                this.world.addEntity(item);

                                handsetEntity = item;

                                world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                            }
                        }
                    }

//                    if(!found){
//                        // couldn't find the handset
//
//                        // this doesn't work on the server
//                        //if(isTheHandset(pl.inventory.getItemStack())){
//                        //    pl.inventory.setItemStack(ItemStack.EMPTY);
//                        //}
//
//                        ItemEntity item = new ItemEntity(this.world, pl.getPosX(), pl.getPosYEye(), pl.getPosZ(), createHandset());
//                        Vector3d vel = new Vector3d(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()).subtract(item.getPositionVec()).scale(0.1);
//                        item.setVelocity(vel.x, vel.y, vel.z);
//                        item.setPickupDelay(40);
//                        item.setThrowerId(pl.getUniqueID());
//                        this.world.addEntity(item);
//
//                        interactingPlayer = null;
//                    }
                }

            }
        }else{
            // client

            if(world.getGameTime() % 20 == 0 && handsetEntity == null && missingHandsetEntityID != -1){
                handsetEntity = world.getEntityByID(missingHandsetEntityID);
            }
        }

        // server & client

        if(handsetEntity != null) {
            float handsetDistance = (float) Math.sqrt(handsetEntity.getDistanceSq(Vector3d.copyCentered(getPos())));

            if (handsetDistance > cordLength) {
                float sFactor = 0f; // spread (higher -> lower result)
                float aFactor = 8f; // inverse amplitude (higher -> lower result)

                float scale = (-((sFactor + cordLength) / (handsetDistance + sFactor)) + 1) / aFactor;
                double d0 = ((this.getPos().getX() + 0.5) - handsetEntity.getPosX()) / (double) handsetDistance;
                double d1 = ((this.getPos().getY() + 0.5) - handsetEntity.getPosY()) / (double) handsetDistance;
                double d2 = ((this.getPos().getZ() + 0.5) - handsetEntity.getPosZ()) / (double) handsetDistance;
                Vector3d add = new Vector3d(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2));
                handsetEntity.setMotion(handsetEntity.getMotion().add(add.normalize().scale(scale)));
            }
        }

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        if(handsetEntity != null) nbt.putInt("handsetEntity", handsetEntity.getEntityId());

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        Preconditions.checkNotNull(world);

        if(nbt.contains("handsetEntity")){
            handsetEntity = world.getEntityByID(nbt.getInt("handsetEntity"));
            if(handsetEntity == null){
                missingHandsetEntityID = nbt.getInt("handsetEntity");
            }else{
                missingHandsetEntityID = -1;
            }
        }else{
            handsetEntity = null;
            missingHandsetEntityID = -1;
        }
    }

    @Override
    public void answerPhone(@Nullable ServerPlayerEntity player) {
        super.answerPhone(player);
        // server side

        Preconditions.checkNotNull(player); // player should never be null for this particular TE

        if(handsetEntity == null){
            pickUpHandset(player);
        }
    }

    public void pickUpHandset(ServerPlayerEntity player) {
        Preconditions.checkNotNull(world);

        handsetEntity = player;

        player.setHeldItem(Hand.MAIN_HAND, createHandset());

        world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 1.0f, 1.0f);

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void reconnectHandset(ServerPlayerEntity player){
        Preconditions.checkNotNull(world);

        handsetEntity = player;

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void disconnectHandset(Entity entityItem) {
        Preconditions.checkNotNull(world);

        handsetEntity = entityItem;

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public boolean isTheHandset(ItemStack stack){
        if(stack.getItem() instanceof HandsetItem && stack.hasTag()) {
            Preconditions.checkNotNull(stack.getTag());
            return stack.getTag().getInt("connected_x") == getPos().getX()
                    && stack.getTag().getInt("connected_y") == getPos().getY()
                    && stack.getTag().getInt("connected_z") == getPos().getZ();
        }

        return false;
    }

    protected ItemStack createHandset(){
        ItemStack stack = new ItemStack(ItemRegister.TELEPHONE_HANDSET.get(), 1);

        HandsetItem.write(stack, this.getPos(), 0xffffff);

        return stack;
    }

    @Nullable
    public Entity getHandsetEntity(){
        return handsetEntity;
    }

    @Override
    public void endCall() {
        super.endCall();

        handsetEntity = null;
    }

    @Override
    public void remove() {
        Preconditions.checkNotNull(world);

        if(!world.isRemote){
            // server

            if(handsetEntity != null){
                if(handsetEntity instanceof HandsetEntity){
                    handsetEntity.remove();
                }else if(handsetEntity instanceof ItemEntity){
                    // backup functionality
                    handsetEntity.remove();
                }else if(handsetEntity instanceof PlayerEntity){
                    int slot = ((PlayerEntity)handsetEntity).inventory.getSlotFor(createHandset()); //TODO: !!! CRASH: getSlotFor is client only but we are the server here
                    if(slot != -1) ((PlayerEntity)handsetEntity).inventory.removeStackFromSlot(slot);
                    if(((PlayerEntity)handsetEntity).getHeldItemOffhand().isItemEqual(createHandset())){
                        ((PlayerEntity)handsetEntity).setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                    }
                }
            }
        }

        super.remove();
    }

    @Nonnull
    public HandSide getHoldingHand(PlayerEntity holder) {
        if(handsetEntity != holder) return holder.getPrimaryHand();

        if(isTheHandset(holder.getHeldItemMainhand())){
            return holder.getPrimaryHand();
        }else if(isTheHandset(holder.getHeldItemOffhand())){
            return holder.getPrimaryHand().opposite();
        }

        return holder.getPrimaryHand();
    }

    @SuppressWarnings("WeakerAccess")
    public Optional<ItemStack> findHandsetItem(){
        if(handsetEntity instanceof HandsetEntity){
            return Optional.of(((HandsetEntity)handsetEntity).getItem());
        }else if(handsetEntity instanceof PlayerEntity){
            PlayerEntity pl = (PlayerEntity)handsetEntity;

            if(isTheHandset(pl.getHeldItemMainhand())){
                return Optional.of(pl.getHeldItemMainhand());
            }else if(isTheHandset(pl.getHeldItemOffhand())){
                return Optional.of(pl.getHeldItemOffhand());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isStillCalling(ICallable other) {
        return handsetEntity != null; //TODO: actually keep track of who I'm dialing
    }

    @Override
    public boolean shouldProvideAudio(PlayerEntity player) {
        return player.equals(handsetEntity);
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
        if(handsetEntity != null){
            return handsetEntity.getPositionVec();
        }

        return Vector3d.copyCentered(pos);
    }

    @Override
    public boolean shouldBeMono() {
        return ImmersiveTelephone.proxy.getLocalPlayer() == handsetEntity;
    }

    @SuppressWarnings("WeakerAccess")
    public Vector3d getCordConnectionPos() {
        Direction side = getBlockState().get(TelephoneBlock.FACING);
        return new Vector3d(0.5 + side.getXOffset() * (7.0/16.0), 2.5 / 16.0, 0.5 + side.getZOffset() * (7.0/16.0));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // disable culling
        return INFINITE_EXTENT_AABB;
    }
}
