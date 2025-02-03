package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.FireBlockInvoker;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EmberEntity extends ThrownEntity {
    /**
     * {@code -1} means has not landed yet
     */
    private int landedTicks = -1;

    // Doesn't need to be synced
    public final float scale = 0.4f * random.nextFloat() + 0.8f;

    public static final int MAX_LANDED_TICKS = 200;

    public EmberEntity(EntityType<? extends ThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public EmberEntity(double x, double y, double z, World world) {
        super(OmniCrossbowEntities.EMBER, x, y, z, world);
    }

    public EmberEntity(LivingEntity owner, World world) {
        super(OmniCrossbowEntities.EMBER, owner, world);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    public void tick() {
        if (!getWorld().getFluidState(getBlockPos()).isEmpty()) {
            for (var i = 0; i < 4; i++)
                // This doesn't seem to be working for some reason
                getWorld().addParticle(ParticleTypes.CLOUD, getX(), getBlockY() + 1, getZ(), 0, 0, 0);
            discard();
            return;
        }
        if (landedTicks < 0) {
            super.tick();
        } else {
            if (landedTicks > MAX_LANDED_TICKS) {
                discard();
                return;
            }
            landedTicks++;
        }
        if (landedTicks < 0 || random.nextFloat() < 0.125) {
            var velocity = getVelocity();
            getWorld().addParticle(ParticleTypes.SMOKE, getX(), getEyeY(), getZ(), velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        setVelocity(0, 0, 0);
        setPosition(blockHitResult.getSide().getAxis() == Axis.Y ? blockHitResult.getPos() : blockHitResult.getPos().add(Vec3d.of(blockHitResult.getSide().getVector()).multiply(getWidth() / 2)));
        if (blockHitResult.getSide() == Direction.UP)
            landedTicks = 0;
        var world = getWorld();
        if (world.isClient) return;
        var blockPos = blockHitResult.getBlockPos();
        var state = world.getBlockState(blockPos);
        var placePos = blockPos.offset(blockHitResult.getSide());
        if (world.getBlockState(placePos).isReplaceable() && state.getFluidState().isEmpty() && random.nextFloat() < (((FireBlockInvoker) Blocks.FIRE).invokeGetBurnChance(state) > 0 ? 0.4f : 0.3f)) {
            world.setBlockState(placePos, ((FireBlockInvoker) Blocks.FIRE).invokeGetStateForPosition(world, placePos));
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity livingEntity && livingEntity.hurtTime > 0) return;
        entity.damage(getWorld().getDamageSources().create(DamageTypes.IN_FIRE, this, getOwner()), 2);
        entity.setOnFireFor(4);
        discard();
    }

    public int getLandedTicks() {
        return landedTicks;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("LandedTicks", landedTicks);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        landedTicks = nbt.getInt("LandedTicks");
    }
}
