package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class DelayedShotEntity extends Entity {
    protected final ItemStack launcher;
    protected final @Nullable LivingEntity owner;
    private int ticksRemaining;
    private boolean shot = false;

    @SuppressWarnings("NullableProblems")
    public DelayedShotEntity(EntityType<? extends DelayedShotEntity> type, World world, LivingEntity owner, ItemStack launcher, int tickDelay) {
        super(type, world);
        this.owner = owner;
        this.launcher = launcher;
        this.ticksRemaining = tickDelay;
        if (owner instanceof PlayerEntity playerEntity) playerEntity.getItemCooldownManager().set(launcher.getItem(), tickDelay);
        setPos(owner.getX(), owner.getEyeY() - 0.4f, owner.getZ());
    }

    public DelayedShotEntity(EntityType<? extends DelayedShotEntity> type, World world) {
        super(type, world);
        this.owner = null;
        this.launcher = ItemStack.EMPTY;
        this.ticksRemaining = 0;
    }

    @Override
    public void tick() {
        prevX = getX();
        prevY = getY();
        prevZ = getZ();

        if (getWorld().isClient) return;

        if (owner == null) {
            discard();
            return;
        }

        setPos(owner.getX(), owner.getEyeY() - 0.4f, owner.getZ());

        if (ticksRemaining > 0) {
            ticksRemaining--;
        } else if (!shot) {
            if (isOwnerHoldingLauncher()) {
                getWorld().playSound(null, getX(), getY(), getZ(), getShootSound(), owner.getSoundCategory(), 1f, 1f);
                onShoot();
                shot = true;
            } else {
                discard();
            }
        }
    }

    protected boolean isOwnerHoldingLauncher() {
        return owner != null && !launcher.isEmpty() && (owner.getStackInHand(Hand.MAIN_HAND) == launcher || owner.getStackInHand(Hand.OFF_HAND) == launcher);
    }

    protected void unloadCrossbow() {
        MultichamberedEnchantment.unloadOneProjectile(launcher);
        if (owner != null) {
            for (var slot : EquipmentSlot.values())
                if (owner.getEquippedStack(slot) == launcher) {
                    launcher.damage(1, owner, slot);
                    return;
                }
        }

        launcher.damage(1, (ServerWorld) getWorld(), null, item -> {});
    }

    public boolean hasShot() {
        return shot;
    }

    // Child classes are responsible for discarding themselves once `onShoot()` is called
    protected abstract void onShoot();

    protected abstract SoundEvent getShootSound();

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    // Same as MarkerEntity

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return PistonBehavior.IGNORE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }
}
