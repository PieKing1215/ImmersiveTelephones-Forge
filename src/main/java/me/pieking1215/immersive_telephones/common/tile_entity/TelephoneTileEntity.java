package me.pieking1215.immersive_telephones.common.tile_entity;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.ConnectorTileHelper;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.item.ItemRegister;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TelephoneTileEntity extends TileEntity implements IImmersiveConnectable, ITickableTileEntity {
    private GlobalWireNetwork globalNet;

    private UUID tel_UUID;
    private String name = "000";
    private int color = 0xffffff;

    private TelephoneTileEntity whoRings = null; // server only

    private long ringTime = -1;

    private boolean inCall = false;

    // TODO: I think storing TEs like this might not be safe
    //       since if it unloads it'll be invalid
    private final List<TelephoneTileEntity> inCallWith = new ArrayList<>();

    private Entity handsetEntity = null;

    private String dial = "";

    private long lastDial = 0;
    private int lastButton = -1;
    private int missingHandsetEntityID = -1;

    // TODO: this whole concept is insanely stupid on so many levels
    @OnlyIn(Dist.CLIENT)
    public Object clientHandItemMatrix4f = null;
    @OnlyIn(Dist.CLIENT)
    public Object clientCameraMatrix4f = null;

    public TelephoneTileEntity() {
        super(TileEntityRegister.TELEPHONE.get());
        tel_UUID = UUID.randomUUID();
    }

    @Override
    public void tick() {
        Preconditions.checkState(world != null);

        if(!world.isRemote) {
            // server
            ServerWorld sw = (ServerWorld) this.world;
            //ServerPlayerEntity pl = interactingPlayer == null ? null : (ServerPlayerEntity) world.getPlayerByUuid(interactingPlayer);

            if(world.getGameTime() % 10 == 0 && !getBlockState().get(TelephoneBlock.HANDSET) && handsetEntity == null){
                // the handset entity got lost

                sw.getLoadedEntitiesWithinAABB(HandsetEntity.class, AxisAlignedBB.fromVector(Vector3d.copyCentered(getPos())).grow(20)).stream()
                        .filter(e -> getPos().equals(HandsetItem.getConnectedPosition(e.getItem())))
                        .findFirst().ifPresent(this::disconnectHandset);

                sw.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, AxisAlignedBB.fromVector(Vector3d.copyCentered(getPos())).grow(20)).stream()
                        .filter(e -> getPos().equals(HandsetItem.getConnectedPosition(e.getHeldItemMainhand()))
                                  || getPos().equals(HandsetItem.getConnectedPosition(e.getHeldItemOffhand())))
                        .findFirst().ifPresent(this::reconnectHandset);
            }

            if(!dial.isEmpty() && world.getGameTime() - lastDial > 20 * 2){
                findConnectedPhones().stream()
                        .filter(t -> t.getName().equals(dial))
                        .findFirst().ifPresent(
                                other -> other.beingCalled(this));
                dial = "";
            }

            if(handsetEntity != null) {

                float f = (float)Math.sqrt(handsetEntity.getDistanceSq(Vector3d.copyCentered(getPos())));
                if(f > 6.0f){

                    float sFactor = 0f; // spread (higher -> lower result)
                    float aFactor = 8f; // inverse amplitude (higher -> lower result)

                    float scale = (-((sFactor + 6) / (f + sFactor)) + 1) / aFactor;
                    double d0 = ((this.getPos().getX() + 0.5) - handsetEntity.getPosX()) / (double) f;
                    double d1 = ((this.getPos().getY() + 0.5) - handsetEntity.getPosY()) / (double) f;
                    double d2 = ((this.getPos().getZ() + 0.5) - handsetEntity.getPosZ()) / (double) f;
                    Vector3d add = new Vector3d(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2));
                    handsetEntity.setMotion(handsetEntity.getMotion().add(add.normalize().scale(scale)));
                }

                if(handsetEntity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity pl = (ServerPlayerEntity) handsetEntity;

                    boolean dropIfPresent = false;
                    if (this.getPos().distanceSq(pl.getPositionVec(), true) > 10 * 10) {
                        dropIfPresent = true;
                    }

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

            if(inCall){
                if(world.getGameTime() % 10 == 0){

                    // green particle line between phones in a call together
                    Vector3d from = new Vector3d(getPos().getX(), getPos().getY(), getPos().getZ());
                    for(TelephoneTileEntity other : inCallWith) {
                        for (float t = 0; t < 1.0f; t += 0.1f) {
                            Vector3d v3 = from.add((new Vector3d(other.getPos().getX(), other.getPos().getY(), other.getPos().getZ()).subtract(from)).mul(t, t, t));
                            v3 = v3.add(0.5, 0.5, 0.5);

                            sw.spawnParticle(ParticleTypes.HAPPY_VILLAGER, v3.x, v3.y, v3.z, 1, 0, 0, 0, 0.0f);
                        }
                    }

                    // hearts above this phone
                    sw.spawnParticle(ParticleTypes.HEART, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, 1, 0, 0, 0, 0.0f);

                }
            }

            if(whoRings != null) {
                if(isRinging()) {
                    long ringTimeLeft = ringTime - world.getGameTime();
                    if (ringTimeLeft % 4 == 0 && ringTimeLeft % 40 < 30) {
                        sw.spawnParticle(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.3, 0.2, 0.1f);
                        sw.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }else if(ringTime == world.getGameTime()){
                    sw.spawnParticle(ParticleTypes.BARRIER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0.0f);
                    sw.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.UI_TOAST_IN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            }
        }else{
            // client

            if(world.getGameTime() % 20 == 0 && handsetEntity == null && missingHandsetEntityID != -1){
                handsetEntity = world.getEntityByID(missingHandsetEntityID);
            }

            if(handsetEntity != null) {

                float f = (float) Math.sqrt(handsetEntity.getDistanceSq(Vector3d.copyCentered(getPos())));
                if (f > 6.0f) {

                    float sFactor = 0f; // spread (higher -> lower result)
                    float aFactor = 8f; // inverse amplitude (higher -> lower result)

                    float scale = (-((sFactor + 6) / (f + sFactor)) + 1) / aFactor;
                    double d0 = ((this.getPos().getX() + 0.5) - handsetEntity.getPosX()) / (double) f;
                    double d1 = ((this.getPos().getY() + 0.5) - handsetEntity.getPosY()) / (double) f;
                    double d2 = ((this.getPos().getZ() + 0.5) - handsetEntity.getPosZ()) / (double) f;
                    Vector3d add = new Vector3d(Math.copySign(d0 * d0 * 0.4D, d0), Math.copySign(d1 * d1 * 0.4D, d1), Math.copySign(d2 * d2 * 0.4D, d2));
                    handsetEntity.setMotion(handsetEntity.getMotion().add(add.normalize().scale(scale)));
                }
            }
        }
    }

    public List<TelephoneTileEntity> findConnectedPhones(){
        List<TelephoneTileEntity> list = new ArrayList<>();

        if(world == null) return list;

        LocalWireNetwork net = GlobalWireNetwork.getNetwork(this.world).getNullableLocalNet(this.getPos());
        if (net == null) return list;

        for(BlockPos p : net.getConnectors()){
            IImmersiveConnectable connect = net.getConnector(p);
            if(connect instanceof TelephoneTileEntity && connect != this){
                list.add((TelephoneTileEntity) connect);
            }
        }

        return list;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putString("name", name);
        nbt.putInt("color", color);
        nbt.putUniqueId("tel_UUID", tel_UUID);

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        name = nbt.getString("name");
        if(nbt.contains("color")) color = nbt.getInt("color");
        if(nbt.contains("tel_UUID")) tel_UUID = nbt.getUniqueId("tel_UUID");

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putString("name", name);
        nbt.putInt("color", color);
        nbt.putUniqueId("tel_UUID", tel_UUID);
        nbt.putLong("ringTime", ringTime);
        nbt.putBoolean("inCall", inCall);
        if(handsetEntity != null) nbt.putInt("handsetEntity", handsetEntity.getEntityId());

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);


        name = nbt.getString("name");
        if(nbt.contains("color")) color = nbt.getInt("color");
        tel_UUID = nbt.getUniqueId("tel_UUID");
        ringTime = nbt.getLong("ringTime");
        inCall = nbt.getBoolean("inCall");

        Preconditions.checkState(world != null);

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

        if(world.isRemote){
            // client side
            ImmersiveTelephone.proxy.registerTelephoneAudioChannel(this);
        }

    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putString("name", name);
        nbt.putInt("color", color);
        nbt.putUniqueId("tel_UUID", tel_UUID);
        nbt.putLong("ringTime", ringTime);
        nbt.putBoolean("inCall", inCall);
        if(handsetEntity != null) nbt.putInt("handsetEntity", handsetEntity.getEntityId());

        return new SUpdateTileEntityPacket(getPos(), -1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbt = pkt.getNbtCompound();

        name = nbt.getString("name");
        if(nbt.contains("color")) color = nbt.getInt("color");
        tel_UUID = nbt.getUniqueId("tel_UUID");
        ringTime = nbt.getLong("ringTime");
        inCall = nbt.getBoolean("inCall");

        Preconditions.checkState(world != null);

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

    public String getName(){
        return name;
    }

    public void setName(String displayName) {
        this.name = displayName;
    }

    @SuppressWarnings("WeakerAccess")
    public long getRingTime(){
        return ringTime;
    }

    public void beingCalled(TelephoneTileEntity calledBy) {
        Preconditions.checkState(world != null);

        if(!world.isRemote){
            // server side

            this.ringTime = world.getGameTime() + 20 * 10;
            this.whoRings = calledBy;

            world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    public boolean isRinging() {
        if(world == null) return false;
        return getRingTime() > world.getGameTime();
    }

    public TelephoneTileEntity getWhoRings(){
        return whoRings;
    }

    public void answerPhone(ServerPlayerEntity player) {
        Preconditions.checkState(world != null);

        ringTime = -1;

        if(whoRings.handsetEntity != null) {

            inCall = true;

            // if I answer and have other people already, add them to the new caller and the new caller to them
            inCallWith.forEach(o -> {
                o.addToCall(whoRings);
                whoRings.addToCall(o);

                // if the caller was also in a group add all of my group to theirs and vice versa
                whoRings.inCallWith.forEach(o2 -> {
                    o.addToCall(o2);
                    o2.addToCall(o);
                });
            });

            inCallWith.add(whoRings);

            // if the caller had other people on the line add them all to us and us to all of them
            whoRings.inCallWith.forEach(o -> {
                addToCall(o);
                o.addToCall(this);

                // if I also have people in a group add all of their group to mine and vice versa
                inCallWith.forEach(o2 -> {
                    o2.addToCall(o);
                    o.addToCall(o2);
                });
            });

            whoRings.addToCall(this);

            whoRings = null;
        }

        if(handsetEntity == null) pickUpHandset(player);

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @SuppressWarnings("WeakerAccess")
    public void addToCall(TelephoneTileEntity other){
        Preconditions.checkState(world != null);

        if(other == this || inCallWith.contains(other)){
            // bonus check because I definitely got the logic in answerPhone wrong
            return;
        }

        inCall = true;
        inCallWith.add(other);

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void pickUpHandset(ServerPlayerEntity player) {
        Preconditions.checkState(world != null);

        handsetEntity = player;

        player.setHeldItem(Hand.MAIN_HAND, createHandset());

        world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 1.0f, 1.0f);

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void reconnectHandset(ServerPlayerEntity player){
        Preconditions.checkState(world != null);

        handsetEntity = player;

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void disconnectHandset(Entity entityItem) {
        Preconditions.checkState(world != null);

        handsetEntity = entityItem;

        if(handsetEntity instanceof HandsetEntity){
            HandsetItem.setColor(((HandsetEntity)handsetEntity).getItem(), this.color);
        }

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, false));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public boolean isTheHandset(ItemStack stack){
        if(stack.getItem() instanceof HandsetItem && stack.hasTag()) {
            Preconditions.checkState(stack.getTag() != null);
            return stack.getTag().getInt("connected_x") == getPos().getX()
                && stack.getTag().getInt("connected_y") == getPos().getY()
                && stack.getTag().getInt("connected_z") == getPos().getZ();
        }

        return false;
    }

    private ItemStack createHandset(){
        ItemStack stack = new ItemStack(ItemRegister.TELEPHONE_HANDSET.get(), 1);

        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("connected_x", this.getPos().getX());
        nbt.putInt("connected_y", this.getPos().getY());
        nbt.putInt("connected_z", this.getPos().getZ());
        nbt.putInt("color", this.getColor());
        stack.setTag(nbt);

        return stack;
    }

    public void endCall() {
        Preconditions.checkState(world != null);

        inCall = false;
        handsetEntity = null;

        for(TelephoneTileEntity te : inCallWith){
            te.leftCall(this);
        }

        inCallWith.clear();

        dial = "";

        world.setBlockState(pos, this.getBlockState().with(TelephoneBlock.HANDSET, true));
        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    private void leftCall(TelephoneTileEntity other){
        inCallWith.remove(other);

        if(inCallWith.isEmpty()){
            inCall = false;
            // don't reset interactingPlayer since out player hasn't hung up yet
        }

    }

    public UUID getUUID() {
        return tel_UUID;
    }

    @Nullable
    public Entity getHandsetEntity(){
        return handsetEntity;
    }

    public List<TelephoneTileEntity> getInCallWith(){
        return inCallWith;
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

    public void pressButton(ServerPlayerEntity player, int i) {
        Preconditions.checkState(world != null);

        if(i == lastButton && world.getGameTime() - lastDial < 5) return;

        // this switch is terrible but whatever
        switch(i){
            case 0:
                dial += "1";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.259921f);
                break;
            case 1:
                dial += "2";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.334840f);
                break;
            case 2:
                dial += "3";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.414214f);
                break;
            case 3:
                dial += "4";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.498307f);
                break;
            case 4:
                dial += "5";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.587401f);
                break;
            case 5:
                dial += "6";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.681793f);
                break;
            case 6:
                dial += "7";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.781797f);
                break;
            case 7:
                dial += "8";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.887749f);
                break;
            case 8:
                dial += "9";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 2.0f);
                break;
            case 10:
                dial += "0";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.189207f);
                break;
            case 9:
                // *
                break;
            case 11:
                // #
                break;
        }

        player.sendMessage(new StringTextComponent("press button " + i + " \"" + dial + "\""), null);

        lastDial = world.getGameTime();
        lastButton = i;
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public BlockPos getConnectionMaster(@Nullable WireType wireType, TargetingInfo targetingInfo) {
        return this.getPos();
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vector3i vector3i) {
        return true;
    }

    @Override
    public void connectCable(WireType wireType, ConnectionPoint connectionPoint, IImmersiveConnectable iImmersiveConnectable, ConnectionPoint connectionPoint1) {

    }

    @Nullable
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo targetingInfo, Vector3i vector3i) {
        return new ConnectionPoint(this.pos, 0);
    }

    @Override
    public void removeCable(@Nullable Connection connection, ConnectionPoint connectionPoint) {
        this.markDirty();
    }

    @Override
    public Vector3d getConnectionOffset(@Nonnull Connection connection, ConnectionPoint connectionPoint) {
        Direction side = getBlockState().get(TelephoneBlock.FACING);
        return new Vector3d(0.5 + side.getXOffset() * (7.0/16.0), 15.0 / 16.0, 0.5 + side.getZOffset() * (7.0/16.0));
    }

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableList.of(new ConnectionPoint(this.pos, 0));
    }

    public void setWorldAndPos(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        super.setWorldAndPos(worldIn, pos);
        this.globalNet = GlobalWireNetwork.getNetwork(worldIn);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        globalNet.onConnectorUnload(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Preconditions.checkState(world != null);

        GlobalWireNetwork.getNetwork(world).onConnectorLoad(this, world);
    }

    @Nonnull
    public IModelData getModelData() {
        Preconditions.checkState(world != null);

        return CombinedModelData.combine(new SinglePropertyModelData<>(ConnectorTileHelper.genConnBlockState(this.world, this), IEProperties.Model.CONNECTIONS), super.getModelData());
    }

    @Override
    public void remove() {
        Preconditions.checkState(world != null);

        if(!world.isRemote){
            // server

            if(handsetEntity != null){
                if(handsetEntity instanceof HandsetEntity){
                    handsetEntity.remove();
                }else if(handsetEntity instanceof ItemEntity){
                    // backup functionality
                    handsetEntity.remove();
                }else if(handsetEntity instanceof PlayerEntity){
                    int slot = ((PlayerEntity)handsetEntity).inventory.getSlotFor(createHandset());
                    if(slot != -1) ((PlayerEntity)handsetEntity).inventory.removeStackFromSlot(slot);
                    if(((PlayerEntity)handsetEntity).getHeldItemOffhand().isItemEqual(createHandset())){
                        ((PlayerEntity)handsetEntity).setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                    }
                }
            }
        }

        super.remove();
        ConnectorTileHelper.remove(this.world, this);
    }

    public int getColor() {
        return color;
    }

    public void dyed(DyeColor color) {
        Preconditions.checkState(world != null);

        float[] newColor = color.getColorComponentValues();

        int oldR = (this.color >> 16 & 255);
        int oldG = (this.color >> 8 & 255);
        int oldB = (this.color & 255);

        int maxColor = Math.max(oldR, Math.max(oldG, oldB));

        int newR = (int) (newColor[0] * 255);
        int newG = (int) (newColor[1] * 255);
        int newB = (int) (newColor[2] * 255);

        maxColor += Math.max(newR, Math.max(newG, newB));

        int combinedR = (oldR + newR) / 2;
        int combinedG = (oldG + newG) / 2;
        int combinedB = (oldB + newB) / 2;

        float f3 = maxColor / 2f;
        float f4 = Math.max(combinedR, Math.max(combinedG, combinedB));
        combinedR = (int)(combinedR * f3 / f4);
        combinedG = (int)(combinedG * f3 / f4);
        combinedB = (int)(combinedB * f3 / f4);

        this.color = (((combinedR << 8) + combinedG) << 8) + combinedB;

        findHandsetItem().ifPresent(is -> HandsetItem.setColor(is, this.color));

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

    }

    public void clearDye() {
        Preconditions.checkState(world != null);

        this.color = 0xffffff;

        findHandsetItem().ifPresent(is -> HandsetItem.setColor(is, this.color));

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
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

}
