package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.util.OmniUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import static net.minecraft.util.math.MathHelper.square;

public class GrappleFishingHookEntity extends ProjectileEntity {

    public static final TrackedData<OptionalInt> HOOKED_ENTITY_ID = DataTracker.registerData(GrappleFishingHookEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    public static final TrackedData<Optional<BlockPos>> HOOKED_BLOCK_POS = DataTracker.registerData(GrappleFishingHookEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    public static final TrackedData<Direction> HOOKED_BLOCK_SIDE = DataTracker.registerData(GrappleFishingHookEntity.class, TrackedDataHandlerRegistry.FACING);
    public static final TrackedData<Boolean> PULLING_OWNER = DataTracker.registerData(GrappleFishingHookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private @Nullable Entity hookedEntity = null;
    private State state = State.FLYING;

    public static double MAX_VELOCITY = 1.0;
    public static double MIN_DISTANCE = 1.5;

    public GrappleFishingHookEntity(EntityType<? extends GrappleFishingHookEntity> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    public GrappleFishingHookEntity(World world, LivingEntity owner) {
        this(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, world);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HOOKED_ENTITY_ID, OptionalInt.empty());
        builder.add(HOOKED_BLOCK_POS, Optional.empty());
        builder.add(HOOKED_BLOCK_SIDE, Direction.UP);
        builder.add(PULLING_OWNER, false);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (data.equals(HOOKED_ENTITY_ID)) {
            var entityId =  dataTracker.get(HOOKED_ENTITY_ID);
            hookedEntity = entityId.isEmpty() ? null : getWorld().getEntityById(entityId.getAsInt());
        }
        super.onTrackedDataSet(data);
    }

    public @Nullable Entity getHookedEntity() {
        return hookedEntity;
    }

    private void setHookedEntity(@Nullable Entity hookedEntity) {
        this.hookedEntity = hookedEntity;
        dataTracker.set(HOOKED_ENTITY_ID, hookedEntity == null ? OptionalInt.empty() : OptionalInt.of(hookedEntity.getId()));
    }

    public @Nullable BlockPos getHookedBlockPos() {
        return dataTracker.get(HOOKED_BLOCK_POS).orElse(null);
    }

    public Direction getHookedBlockSide() {
        return dataTracker.get(HOOKED_BLOCK_SIDE);
    }

    private void setHookedBlock(@Nullable BlockPos blockPos, @Nullable Direction side) {
        dataTracker.set(HOOKED_BLOCK_POS, Optional.ofNullable(blockPos));
        dataTracker.set(HOOKED_BLOCK_SIDE, Objects.requireNonNullElse(side, Direction.UP));
    }

    public boolean isPullingOwner() {
        return dataTracker.get(PULLING_OWNER);
    }

    private void setPullingOwner(boolean isPullingOwner) {
        dataTracker.set(PULLING_OWNER, isPullingOwner);
    }

    public @Nullable LivingEntity getLivingOwner() {
        var owner = getOwner();
        return owner instanceof LivingEntity livingEntity ? livingEntity : null;
    }


    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity instanceof Grappler grappler)
            grappler.omnicrossbow$setHook(this);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!getWorld().isClient)
            setHookedBlock(blockHitResult.getBlockPos(), blockHitResult.getSide());
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!getWorld().isClient)
            setHookedEntity(entityHitResult.getEntity());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (getOwner() instanceof Grappler grappler)
            grappler.omnicrossbow$setHook(null);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (getOwner() instanceof Grappler grappler)
            grappler.omnicrossbow$setHook(null);

        super.remove(reason);
    }

    @Override
    public boolean shouldRender(double distance) {
        return getLivingOwner() instanceof PlayerEntity player && player.isMainPlayer() || super.shouldRender(distance);
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    public void tick() {
        super.tick();

        var owner = getLivingOwner();

        if (owner == null) {
            discard();
            return;
        }

        if (!getWorld().isClient && removeIfInvalid(owner))
            return;

        if (state == State.FLYING) {
            if (getHookedEntity() != null) {
                setVelocity(Vec3d.ZERO);
                state = State.HOOKED_IN_ENTITY;
                return;
            }

            if (getHookedBlockPos() != null)  {
                setPosition(getHookedBlockPos().toCenterPos().offset(getHookedBlockSide(), 0.5));
                setVelocity(Vec3d.ZERO);
                state = State.HOOKED_IN_BLOCK;
                return;
            }

            if (!getWorld().isClient)
                hitOrDeflect(ProjectileUtil.getCollision(this, this::canHit));

            var velocity = getVelocity();
            setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
            updateRotation();
            refreshPosition();
            return;
        }

        var hookedEntity = getHookedEntity();
        var pullingOwner = state == State.HOOKED_IN_BLOCK || hookedEntity == null || hookedEntity instanceof LivingEntity livingEntity && isHeavier(livingEntity, owner);
        setPullingOwner(pullingOwner);
        var movedEntity = pullingOwner ? owner : hookedEntity;
        movedEntity.dismountVehicle();
        var targetPos = pullingOwner ? this.getPos() : owner.getEyePos();
        var offset = targetPos.subtract((state == State.HOOKED_IN_BLOCK && getHookedBlockSide() == Direction.UP) ? movedEntity.getPos() : movedEntity.getEyePos());
        if (offset.lengthSquared() < square(MIN_DISTANCE)) {
            if (getWorld().isClient) return;

            if (state == State.HOOKED_IN_BLOCK && getHookedBlockSide().getAxis().isHorizontal() && offset.y <= 1f) {
                var ownerVelocity = owner.getVelocity();
                if (ownerVelocity.y < 0.8) {
                    owner.setVelocity(ownerVelocity.x, 0.8, ownerVelocity.z);
                    owner.velocityModified = true;
                }
            }

            unloadCrossbow();
            return;
        }
        var direction = offset.normalize();
        var directionVelocity = movedEntity.getVelocity().dotProduct(direction);
        if (movedEntity.isLogicalSideForUpdatingMovement() && directionVelocity < MAX_VELOCITY)
            movedEntity.addVelocity(direction.multiply((MAX_VELOCITY - directionVelocity) * 0.5));

        if (state == State.HOOKED_IN_BLOCK) {
            if (!getWorld().isClient && getWorld().raycast(new RaycastContext(
                    getPos().offset(getHookedBlockSide(), -0.0625),
                    getPos().offset(getHookedBlockSide(), 1.0),
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    this
            )).getType() == HitResult.Type.MISS)
                unloadCrossbow();
        } else { // if (state == State.HOOKED_IN_ENTITY) {
            if (getHookedEntity() == null) return;

            if (getHookedEntity().isAlive() && getHookedEntity().getWorld().getRegistryKey() == getWorld().getRegistryKey()) {
                setPosition(getHookedEntity().getX(), getHookedEntity().getBodyY(0.8), getHookedEntity().getZ());
            } else if (!getWorld().isClient)
                unloadCrossbow();
        }
    }

    private boolean removeIfInvalid(LivingEntity owner) {
        if (!owner.isRemoved() && owner.isAlive()
                && (hasFishingRodLoaded(owner.getMainHandStack()) || hasFishingRodLoaded(owner.getOffHandStack())))
            return false;

        discard();
        return true;
    }

    public void unloadCrossbow() {
        if (!(getWorld() instanceof ServerWorld serverWorld) || !(getOwner() instanceof LivingEntity livingEntity)) {
            discard();
            return;
        }
        var crossbow = getFishingRodCrossbow(livingEntity);
        if (crossbow.isEmpty()) return;
        var projectile = OmniUtil.getMainProjectile(crossbow);
        projectile.damage(1, serverWorld, null, item -> {});
        livingEntity.dropStack(projectile, livingEntity.getEyeHeight(livingEntity.getPose()) - 0.1f);
        MultichamberedEnchantment.unloadOneProjectile(crossbow);
        crossbow.damage(1, serverWorld, null, item -> {});
        discard();
    }

    public static boolean hasFishingRodLoaded(ItemStack maybeCrossbow) {
        return maybeCrossbow.getItem() instanceof CrossbowItem && OmniUtil.getMainProjectile(maybeCrossbow).isOf(Items.FISHING_ROD);
    }

    public static boolean isHeavier(LivingEntity target, LivingEntity shooter) {
        return target.getWidth() * target.getHeight() * (0.2 + target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                > 1.5 * shooter.getWidth() * shooter.getHeight() * (0.2 + shooter.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
    }

    public static ItemStack getFishingRodCrossbow(LivingEntity livingEntity) {
        var mainStack = livingEntity.getMainHandStack();
        if (hasFishingRodLoaded(mainStack)) return mainStack;
        var offStack = livingEntity.getOffHandStack();
        if (hasFishingRodLoaded(offStack)) return offStack;
        return ItemStack.EMPTY;
    }

    enum State {
        FLYING, HOOKED_IN_BLOCK, HOOKED_IN_ENTITY
    }
}
