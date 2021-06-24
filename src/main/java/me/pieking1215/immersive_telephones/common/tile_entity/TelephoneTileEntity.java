package me.pieking1215.immersive_telephones.common.tile_entity;

import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.TelephoneBlock;
import me.pieking1215.immersive_telephones.common.entity.HandsetEntity;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.item.ItemRegister;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TelephoneTileEntity extends HandsetPhoneTileEntity implements ICallable, IAudioReceiver, IAnimatable {
    private final AnimationFactory manager = new AnimationFactory(this);

    private int color = 0xffffff;

    private String keypadInput = "";

    private long lastKeypadInputTime = 0;
    private int lastKeypadInputIndex = -1;

    public TelephoneTileEntity() {
        super(TileEntityRegister.TELEPHONE.get());
        cordLength = 6;
    }

    @Override
    public void tick() {
        super.tick();
        Preconditions.checkNotNull(world);

        if(!world.isRemote) {
            // server
            ServerWorld sw = (ServerWorld) this.world;
            //ServerPlayerEntity pl = interactingPlayer == null ? null : (ServerPlayerEntity) world.getPlayerByUuid(interactingPlayer);

            if(!keypadInput.isEmpty() && world.getGameTime() - lastKeypadInputTime > 20 * 2){
                dial(keypadInput);
            }

            if(whoRings != null) {
                long ringTimeLeft = ringTime - world.getGameTime();
                if (ringTimeLeft % 4 == 0 && ringTimeLeft % 40 < 30) {
//                    sw.spawnParticle(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.3, 0.2, 0.1f);
//                    sw.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putInt("color", color);

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        if(nbt.contains("color")) color = nbt.getInt("color");

    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();

        nbt.putInt("color", color);

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        super.handleUpdateTag(state, nbt);

        if(nbt.contains("color")) color = nbt.getInt("color");

        Preconditions.checkNotNull(world);

        if(world.isRemote){
            // client side
            ImmersiveTelephone.proxy.registerTelephoneAudioChannel(this);
        }

    }

    @Override
    public void endCall() {
        super.endCall();

        keypadInput = "";
    }

    public void pressButton(ServerPlayerEntity player, int i) {
        Preconditions.checkNotNull(world);

        if(i == lastKeypadInputIndex && world.getGameTime() - lastKeypadInputTime < 5) return;

        // this switch is terrible but whatever
        switch(i){
            case 0:
                keypadInput += "1";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.259921f);
                break;
            case 1:
                keypadInput += "2";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.334840f);
                break;
            case 2:
                keypadInput += "3";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.414214f);
                break;
            case 3:
                keypadInput += "4";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.498307f);
                break;
            case 4:
                keypadInput += "5";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.587401f);
                break;
            case 5:
                keypadInput += "6";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.681793f);
                break;
            case 6:
                keypadInput += "7";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.781797f);
                break;
            case 7:
                keypadInput += "8";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.887749f);
                break;
            case 8:
                keypadInput += "9";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 2.0f);
                break;
            case 10:
                keypadInput += "0";
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 0.5f, 1.189207f);
                break;
            case 9:
                // *
                break;
            case 11:
                // #
                break;
        }

        player.sendMessage(new StringTextComponent("press button " + i + " \"" + keypadInput + "\""), Util.DUMMY_UUID);

        lastKeypadInputTime = world.getGameTime();
        lastKeypadInputIndex = i;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void disconnectHandset(Entity entityItem) {
        super.disconnectHandset(entityItem);

        if(entityItem instanceof HandsetEntity){
            HandsetItem.setColor(((HandsetEntity)entityItem).getItem(), this.color);
        }
    }

    @Override
    protected ItemStack createHandset() {
        ItemStack stack = new ItemStack(ItemRegister.TELEPHONE_HANDSET.get(), 1);

        HandsetItem.write(stack, this.getPos(), this.getColor());

        return stack;
    }

    public void dyed(DyeColor color) {
        Preconditions.checkNotNull(world);

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
        Preconditions.checkNotNull(world);

        this.color = 0xffffff;

        findHandsetItem().ifPresent(is -> HandsetItem.setColor(is, this.color));

        world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    protected void onRingingCancelled() {
        super.onRingingCancelled();
        Preconditions.checkNotNull(world);

        if(!world.isRemote) {
            // server side

            ServerWorld sw = (ServerWorld) world;

            // call missed or cancelled
            sw.spawnParticle(ParticleTypes.BARRIER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0.0f);
            sw.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.UI_TOAST_IN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public void dial(String id) {
        super.dial(id);
        this.keypadInput = "";
    }

    //region <gecko>

    private boolean _prevHandsetState = false;
    private boolean _prevHandsetState_set = false;
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        AnimationController<?> controller = event.getController();

        if(controller.getName().equals("ringer")) {
            event.getController().transitionLengthTicks = 0;
            if(getBlockState().get(TelephoneBlock.HANDSET) && isRinging()){
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.telephone_block.ringing", true));
                return PlayState.CONTINUE;
            }

            return PlayState.STOP;
        }

        if(controller.getName().equals("handset")){
            event.getController().transitionLengthTicks = 0;

            boolean nowHandset = getBlockState().get(TelephoneBlock.HANDSET);
            if(!_prevHandsetState_set){

                if(!nowHandset){
                    event.getController().markNeedsReload();
                    event.getController().setAnimation(new AnimationBuilder()
                            .addAnimation("animation.telephone_block.hide_handset.hold", true));
                }

                _prevHandsetState = nowHandset;
                _prevHandsetState_set = true;
            }

            if(!nowHandset && _prevHandsetState) {
                event.getController().markNeedsReload();
                event.getController().setAnimation(new AnimationBuilder()
                        .addAnimation("animation.telephone_block.hide_handset", false)
                        .addAnimation("animation.telephone_block.hide_handset.hold", true));
            }else if(nowHandset && !_prevHandsetState){

                boolean earlySlammedHandset = false; // since slammedHandset isn't synced until later

                if(getHandsetEntity() instanceof PlayerEntity) earlySlammedHandset = !getHandsetEntity().isOnGround();

                event.getController().markNeedsReload();
                event.getController().setAnimation(new AnimationBuilder()
                        .addAnimation(earlySlammedHandset ? "animation.telephone_block.place_handset.slam"
                                : "animation.telephone_block.place_handset", false));
            }

            _prevHandsetState = nowHandset;

            if(controller.getAnimationState() == AnimationState.Stopped) return PlayState.STOP;

            return PlayState.CONTINUE;
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<?> ringController = new AnimationController<>(this, "ringer", 0, this::predicate);
        animationData.addAnimationController(ringController);
        ringController.registerSoundListener(this::soundListener);

        AnimationController<?> handsetController = new AnimationController<>(this, "handset", 0, this::predicate);
        animationData.addAnimationController(handsetController);
        handsetController.registerSoundListener(this::soundListener);
    }

    private <A extends IAnimatable> void soundListener(SoundKeyframeEvent<A> event) {
        PlayerEntity pl = ImmersiveTelephone.proxy.getLocalPlayer();
        if(!(pl instanceof ClientPlayerEntity)) return;

        if(event.getController().getName().equals("ringer")) {
            //sw.spawnParticle(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.3, 0.2, 0.1f);
            ((ClientPlayerEntity) pl).worldClient.playSound(getPos(), SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        }else if(event.getController().getName().equals("handset")){
            if(event.getController().getCurrentAnimation().animationName.startsWith("animation.telephone_block.place_handset")) {
                ((ClientPlayerEntity) pl).worldClient.playSound(getPos(), event.getController().getCurrentAnimation().animationName.endsWith(".slam") ? SoundEvents.BLOCK_ANVIL_LAND : SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return manager;
    }

    //endregion
}
