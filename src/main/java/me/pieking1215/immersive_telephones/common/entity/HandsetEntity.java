package me.pieking1215.immersive_telephones.common.entity;

import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

@SuppressWarnings("EntityConstructor")
public class HandsetEntity extends ItemEntity {

    HandsetEntity(EntityType<? extends HandsetEntity> p_i50217_1_, World world) {
        super(p_i50217_1_, world);
        this.recenterBoundingBox();
    }

    private HandsetEntity(World worldIn, double x, double y, double z) {
        this(EntityRegister.HANDSET.get(), worldIn);
        this.setPosition(x, y, z);
        this.rotationYaw = this.rand.nextFloat() * 360.0F;
        this.setMotion(this.rand.nextDouble() * 0.2D - 0.1D, 0.2D, this.rand.nextDouble() * 0.2D - 0.1D);
    }

    public HandsetEntity(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
        this.lifespan = stack.getEntityLifespan(worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        lifespan = Integer.MAX_VALUE; // never expire TODO: hacky

        float speed = (float) new Vector3d(getMotion().x, 0, getMotion().z).length() * 32;

        if(speed > 0.25f && rand.nextFloat() < speed) {
            int i = MathHelper.floor(this.getPosX());
            int j = MathHelper.floor(this.getPosY() - (double) 0.2F);
            int k = MathHelper.floor(this.getPosZ());
            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState blockstate = this.world.getBlockState(blockpos);
            if (!blockstate.addRunningEffects(world, blockpos, this)) {
                if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                    Vector3d vector3d = this.getMotion();
                    this.world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate).setPos(blockpos), this.getPosX() + (this.rand.nextDouble() - 0.5D) * (double) this.getType().getSize().width, this.getPosY() + 0.1D, this.getPosZ() + (this.rand.nextDouble() - 0.5D) * (double) this.getType().getSize().width, vector3d.x * -4.0D, 0.5D, vector3d.z * -4.0D);
                }
            }
        }

    }

    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public float getItemHover(float partialTicks) {
        return rotationYaw;
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        // do not do the typical item pick up
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if(player.getHeldItemMainhand().isEmpty()){
            if (!this.world.isRemote) {
                ItemStack itemstack = this.getItem();
                Item item = itemstack.getItem();
                int i = itemstack.getCount();

                player.setHeldItem(Hand.MAIN_HAND, itemstack);
                net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerItemPickupEvent(player, this, itemstack);

                player.onItemPickup(this, i);

                HandsetItem.findConnectedTE(itemstack, this.world).ifPresent(tel ->
                        tel.reconnectHandset((ServerPlayerEntity) player));

                this.remove();

                player.addStat(Stats.ITEM_PICKED_UP.get(item), i);
                player.triggerItemPickupTrigger(this);

                return ActionResultType.CONSUME;
            }else{
                return ActionResultType.SUCCESS;
            }
        }

        return this.world.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
    }

    /**
     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        this.setRawPosition(x, y, z);
        this.setBoundingBox(getType().getSize().func_242285_a(x, y, z));
    }

    /**
     * Recomputes this entity's bounding box so that it is positioned at this entity's X/Y/Z.
     */
    protected void recenterBoundingBox() {
        this.setPosition(this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source != DamageSource.OUT_OF_WORLD;
    }

    @Override
    public Vector3d getLeashStartPosition() {
        return new Vector3d(0.0D, 0.05f, this.getWidth() * 0.4F);
    }

    @Override
    public Vector3d getLeashPosition(float partialTicks) {
        return getLeashHandPosition(partialTicks);
    }
}
