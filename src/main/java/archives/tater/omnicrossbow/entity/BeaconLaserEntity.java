package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.util.RaycastUtil;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class BeaconLaserEntity extends DelayedShotEntity {
    public static final TrackedData<Float> DISTANCE = DataTracker.registerData(BeaconLaserEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> FIRING_TICKS = DataTracker.registerData(BeaconLaserEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int MAX_FIRING_TICKS = 40;
    public static final int MAX_DISTANCE = 128;
    public static final double MARGIN = 0.2;
    public static final double PARTICLE_RADIUS = 1.2;

    public BeaconLaserEntity(EntityType<? extends DelayedShotEntity> type, World world) {
        super(type, world);
        ignoreCameraFrustum = true;
    }

    public BeaconLaserEntity(World world, LivingEntity owner, ItemStack launcher) {
        super(OmniCrossbowEntities.BEACON_LASER, world, owner, launcher, 14);
        ignoreCameraFrustum = true;
        updateAnglesAndDistance();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DISTANCE, 0f);
        builder.add(FIRING_TICKS, 0);
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

        if (owner instanceof CrossbowUser crossbowUser) {
            if (crossbowUser.getTarget() != null)
                lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, crossbowUser.getTarget().getEyePos());
        } else {
            prevYaw = getHeadYaw();
            prevPitch = getPitch();
            setYaw(owner.getYaw());
            setPitch(owner.getPitch());
        }

        var start = getPos();
        var hitResult = getWorld().raycast(new RaycastContext(start, start.add(getRotationVector().multiply(MAX_DISTANCE)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
        setDistance((float) hitResult.getPos().distanceTo(start) + 1);
    }

    @Override
    public void tick() {
        super.tick();

        var firingTicks = getFiringTicks();

        if (getWorld().isClient) {
            if (firingTicks <= 0) return;

            var pos = getPos();
            var step = getRotationVector();
            var world = getWorld();

            var distance = getDistance();
            for (var i = 0; i < distance; i++) {
                if (random.nextFloat() > 0.1 - 0.08 * Math.min(1, i / 12.0)) continue;
                var stepPos = pos.add(step.multiply(i));
                var velocity = step.multiply(2 * random.nextDouble());
                world.addParticle(ParticleTypes.END_ROD,
                        stepPos.x + PARTICLE_RADIUS * (random.nextDouble() - 0.5),
                        stepPos.y + PARTICLE_RADIUS * (random.nextDouble() - 0.5),
                        stepPos.z + PARTICLE_RADIUS * (random.nextDouble() - 0.5),
                        velocity.x,
                        velocity.y,
                        velocity.z);
            }

            return;
        }

        if (!hasShot()) return;

        if (!isOwnerHoldingLauncher()) {
            unloadCrossbow();
            if (owner instanceof PlayerEntity playerEntity)
                playerEntity.getItemCooldownManager().set(launcher.getItem(), 120);
            discard();
            return;
        }

        if (owner != null && firingTicks >= 0) {
            owner.addVelocity(owner.getRotationVector().multiply(-0.05, -0.025, -0.05));
            owner.velocityModified = true;
        }

        if (firingTicks >= MAX_FIRING_TICKS) {
            unloadCrossbow();
            if (owner instanceof PlayerEntity playerEntity)
                playerEntity.getItemCooldownManager().set(launcher.getItem(), 120);
            this.discard();
            return;
        }

        var source = owner == null ? this : owner;

        for (var target : RaycastUtil.pierce(getWorld(), getPos(), getPos().add(getRotationVector().multiply(getDistance())), MARGIN, owner, entity -> entity instanceof LivingEntity || entity instanceof EndCrystalEntity)) {
            var damage = (target instanceof LivingEntity livingEntity ? MathHelper.clamp(livingEntity.getMaxHealth() / 40, 0.5f, 7.5f)  : 0.5f)
                    + (target.getType().isIn(OmniCrossbow.EXTRA_BEACON_DAMAGE_TAG) ? 0.1f : 0);
            target.damage(getWorld().getDamageSources().create(OmniCrossbow.BEACON_DAMAGE, source, source), damage);
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
