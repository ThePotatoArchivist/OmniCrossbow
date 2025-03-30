package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.mixin.FishingBobberEntityAccessor;
import archives.tater.omnicrossbow.util.OmniUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.math.MathHelper.square;

public class GrappleFishingHookEntity extends FishingBobberEntity {
    private final FishingBobberEntityAccessor thisAccess = (FishingBobberEntityAccessor) this;
    private State state = State.FLYING;
    private boolean pullingOwner = false;

    public static double MAX_VELOCITY = 1.0;
    public static double MIN_DISTANCE = 1.5;

    public GrappleFishingHookEntity(EntityType<? extends FishingBobberEntity> type, World world) {
        super(type, world);
        setNoGravity(true);
    }

    public GrappleFishingHookEntity(World world, LivingEntity owner) {
        this(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, world);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    private @Nullable LivingEntity getLivingOwner() {
        var owner = getOwner();
        return owner instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public boolean isPullingOwner() {
        return pullingOwner;
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
        setPosition(blockHitResult.getPos());
        state = State.HOOKED_IN_BLOCK;
    }

    @Override
    public void remove(RemovalReason reason) {
        var owner = getOwner();

        if (owner instanceof Grappler grappler)
            grappler.omnicrossbow$setHook(null);

        super.remove(reason);
    }

    @Override
    public void tick() {
        // See FishingBobberEntityMixin
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

            thisAccess.invokeCheckForCollision();

            if (state != State.FLYING)
                return;
            move(MovementType.SELF, getVelocity());
            updateRotation();
            refreshPosition();
            return;
        }

        var hookedEntity = getHookedEntity();
        pullingOwner = state == State.HOOKED_IN_BLOCK || hookedEntity == null || hookedEntity instanceof LivingEntity livingEntity && isHeavier(livingEntity, owner);
        var movedEntity = pullingOwner ? owner : hookedEntity;
        movedEntity.dismountVehicle();
        var offset = (pullingOwner ? this.getPos() : owner.getEyePos()).subtract(movedEntity.getEyePos());
        if (offset.lengthSquared() < square(MIN_DISTANCE)) {
            if (state == State.HOOKED_IN_BLOCK) {
                var velocity = owner.getVelocity();
                if (velocity.y < 0.4) {
                    owner.setVelocity(velocity.x, 0.6, velocity.z);
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
            if (ProjectileUtil.getCollision(this, this::canHit).getType() == HitResult.Type.MISS) {
                unloadCrossbow();
            }
        } else if (state == State.HOOKED_IN_ENTITY) {
            if (getHookedEntity() == null) return;

            if (getHookedEntity().isAlive() && getHookedEntity().getWorld().getRegistryKey() == getWorld().getRegistryKey()) {
                setPosition(getHookedEntity().getX(), getHookedEntity().getBodyY(0.8), getHookedEntity().getZ());
            } else {
                thisAccess.invokeUpdateHookedEntityId(null);
                unloadCrossbow();
            }
        }
    }

    // TODO rename
    private boolean removeIfInvalid(LivingEntity player) {
        if (!player.isRemoved() && player.isAlive()
                && (hasFishingRodLoaded(player.getMainHandStack()) || hasFishingRodLoaded(player.getOffHandStack())
                && !(squaredDistanceTo(player) > 32 * 32)))
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
