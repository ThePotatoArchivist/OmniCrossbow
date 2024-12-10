package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.util.RaycastUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BeaconLaserEntity extends DelayedShotEntity {
    public static final TrackedData<Float> DISTANCE = DataTracker.registerData(BeaconLaserEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> FIRING_TICKS = DataTracker.registerData(BeaconLaserEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int MAX_FIRING_TICKS = 40;
    public static final int MAX_DISTANCE = 64;
    public static final double MARGIN = 0.2;

    public BeaconLaserEntity(EntityType<? extends DelayedShotEntity> type, World world) {
        super(type, world);
    }

    public BeaconLaserEntity(World world, LivingEntity owner, ItemStack launcher) {
        super(OmniCrossbowEntities.BEACON_LASER, world, owner, launcher, 14);
        updateAnglesAndDistance();
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(DISTANCE, 0f);
        dataTracker.startTracking(FIRING_TICKS, 0);
    }

    public float getDistance() {
        return dataTracker.get(DISTANCE);
    }

    private void setDistance(float distance) {
        dataTracker.set(DISTANCE, distance);
    }

    public int getFiringTicks() {
        return dataTracker.get(FIRING_TICKS);
    }

    private void setFiringTicks(int firingTicks) {
        dataTracker.set(FIRING_TICKS, firingTicks);
    }

    private void updateAnglesAndDistance() {
        if (owner == null) return;

        prevYaw = getHeadYaw();
        prevPitch = getPitch();
        setYaw(owner.getYaw());
        setPitch(owner.getPitch());

        var start = getPos();
        var hitResult = getWorld().raycast(new RaycastContext(start, start.add(getRotationVector().multiply(MAX_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        setDistance((float) hitResult.getPos().distanceTo(start) + 1);
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasShot()) return;

        var firingTicks = getFiringTicks();

        if (owner != null && firingTicks >= 0) {
            owner.addVelocity(owner.getRotationVector().multiply(-0.05, -0.025, -0.05));
            owner.velocityModified = true;
        }

        if (!getWorld().isClient) {

            if (firingTicks >= MAX_FIRING_TICKS) {
                unloadCrossbow();
                if (owner instanceof PlayerEntity playerEntity)
                    playerEntity.getItemCooldownManager().set(launcher.getItem(), 120);
                this.discard();
                return;
            }

            var source = owner == null ? this : owner;

            for (var target : RaycastUtil.pierce(getWorld(), getPos(), getPos().add(getRotationVector().multiply(getDistance())), MARGIN, owner, entity -> entity instanceof LivingEntity || entity instanceof EndCrystalEntity)) {
                var damage = target instanceof LivingEntity livingEntity ? Math.max(0.5f, 0.5f * (float) Math.log(livingEntity.getMaxHealth()) - 1) : 0.5f;
                target.damage(getWorld().getDamageSources().create(OmniCrossbow.BEACON_DAMAGE, source, source), damage);
            }
        }

        updateAnglesAndDistance();

        setFiringTicks(firingTicks + 1);
    }

    @Override
    protected void onShoot() {
        if (owner instanceof PlayerEntity playerEntity) {
            playerEntity.getItemCooldownManager().set(launcher.getItem(), MAX_FIRING_TICKS);
        }
    }

    @Override
    protected SoundEvent getShootSound() {
        return OmniCrossbow.BEACON_FIRE;
    }
}
