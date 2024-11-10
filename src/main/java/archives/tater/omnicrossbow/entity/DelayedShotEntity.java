package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.CrossbowItemInvoker;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
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
    }

    public DelayedShotEntity(EntityType<? extends DelayedShotEntity> type, World world) {
        super(type, world);
        this.owner = null;
        this.launcher = ItemStack.EMPTY;
        this.ticksRemaining = 0;
    }

    @Override
    public void tick() {
        if (getWorld().isClient) return;
        if (owner == null) {
            discard();
            return;
        }
        setPos(owner.getX(), owner.getY(), owner.getZ());

        if (ticksRemaining > 0) {
            ticksRemaining--;
        } else if (!shot) {
            if (!launcher.isEmpty() && (owner.getStackInHand(Hand.MAIN_HAND) == launcher || owner.getStackInHand(Hand.OFF_HAND) == launcher)) {
                getWorld().playSound(null, getX(), getY(), getZ(), getShootSound(), owner.getSoundCategory(), 1f, 1f);
                onShoot();
                shot = true;
            } else {
                discard();
            }
        }
    }

    protected void unloadCrossbow() {
        CrossbowItemInvoker.invokeClearProjectiles(launcher);
        CrossbowItem.setCharged(launcher, false);
    }

    // Child classes are responsible for discarding themselves once `onShoot()` is called
    protected abstract void onShoot();

    protected abstract SoundEvent getShootSound();

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    // Same as MarkerEntity

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        throw new IllegalStateException("Markers should never be sent");
    }

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
