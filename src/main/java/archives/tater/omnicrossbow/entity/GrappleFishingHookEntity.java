package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.FishingBobberEntityAccessor;
import archives.tater.omnicrossbow.util.OmniUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GrappleFishingHookEntity extends FishingBobberEntity {
    private final FishingBobberEntityAccessor thisAccess = (FishingBobberEntityAccessor) this;
    private State state = State.FLYING;

    public static double MAX_VELOCITY = 0.2;

    public GrappleFishingHookEntity(EntityType<? extends FishingBobberEntity> type, World world) {
        super(type, world);
        setNoGravity(true);
    }

    public GrappleFishingHookEntity(World world, LivingEntity owner) {
        this(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, world);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        setPosition(blockHitResult.getPos());
        state = State.HOOKED_IN_BLOCK;
    }

    private @Nullable LivingEntity getLivingOwner() {
        var owner = getOwner();
        return owner instanceof LivingEntity livingEntity ? livingEntity : null;
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

        thisAccess.invokeCheckForCollision();

        if (state == State.FLYING) {
            if (getHookedEntity() != null) {
                setVelocity(Vec3d.ZERO);
                state = State.HOOKED_IN_ENTITY;
                return;
            }

            move(MovementType.SELF, getVelocity());
            updateRotation();
            if (state == State.FLYING && (isOnGround() || horizontalCollision)) {
                setVelocity(Vec3d.ZERO);
            }

            refreshPosition();
            return;
        }

        var hookedEntity = getHookedEntity();
        var ownerMoved = state == State.HOOKED_IN_BLOCK || hookedEntity == null || hookedEntity instanceof LivingEntity livingEntity && isHeavier(livingEntity, owner);
        var movedEntity = ownerMoved ? owner : hookedEntity;
        movedEntity.dismountVehicle();
        var direction = (ownerMoved ? this.getPos() : owner.getEyePos()).subtract(movedEntity.getEyePos()).normalize();
        var directionVelocity = movedEntity.getVelocity().dotProduct(direction);
        if (movedEntity.isLogicalSideForUpdatingMovement() && directionVelocity < MAX_VELOCITY) {
            movedEntity.addVelocity(direction.multiply((MAX_VELOCITY - directionVelocity) * 0.5));
        }

        if (state == State.HOOKED_IN_BLOCK) {
            if (!horizontalCollision && !verticalCollision) {
                discard();
            }
        } else if (state == State.HOOKED_IN_ENTITY) {
            if (getHookedEntity() == null) return;

            if (getHookedEntity().isAlive() && getHookedEntity().getWorld().getRegistryKey() == getWorld().getRegistryKey()) {
                setPosition(getHookedEntity().getX(), getHookedEntity().getBodyY(0.8), getHookedEntity().getZ());
            } else {
                thisAccess.invokeUpdateHookedEntityId(null);
                discard();
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

    public static boolean hasFishingRodLoaded(ItemStack maybeCrossbow) {
        return maybeCrossbow.getItem() instanceof CrossbowItem && OmniUtil.getMainProjectile(maybeCrossbow).isOf(Items.FISHING_ROD);
    }

    public static boolean isHeavier(LivingEntity target, LivingEntity shooter) {
        return target.getWidth() * target.getHeight() * (1 + target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                > 1.5 * shooter.getWidth() * shooter.getHeight() * (1 + shooter.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
    }

    enum State {
        FLYING, HOOKED_IN_BLOCK, HOOKED_IN_ENTITY
    }
}
