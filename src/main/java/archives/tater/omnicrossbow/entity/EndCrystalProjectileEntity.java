package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EndCrystalProjectileEntity extends ExplosiveProjectileEntity {
    public EndCrystalProjectileEntity(EntityType<? extends EndCrystalProjectileEntity> entityType, World world) {
        super(entityType, world);
        accelerationPower = 0;
    }

    public EndCrystalProjectileEntity(World world, LivingEntity shooter, Vec3d direction) {
        super(OmniCrossbowEntities.END_CRYSTAL_PROJECTILE, shooter, Vec3d.ZERO, world);
        accelerationPower = 0;
        setVelocity(direction);
    }

    @Override
    public void tick() {
        super.tick();
        var world = getWorld();
        if (world.isClient) {
            for (int i = 0; i < 4; i++) {
                world.addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), 1.2 * random.nextDouble() - 0.6, 1.2 * random.nextDouble() - 0.6, 1.2 * random.nextDouble() - 0.6);
            }
        }
    }

    @Override
    protected @Nullable ParticleEffect getParticleType() {
        return null;
    }

    @Override
    protected float getDrag() {
        return 1;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        explode();
    }

    private static final ProjectileDeflection DEFLECTION = (projectile, hitEntity, random1) -> {
        projectile.addVelocity(hitEntity instanceof ProjectileEntity ? hitEntity.getVelocity().normalize() : hitEntity != null ? hitEntity.getRotationVector() : projectile.getVelocity().multiply(-1));
    };

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable Entity owner, boolean fromAttack) {
        if (fromAttack && (deflector == null || !deflector.isLiving() || squaredDistanceTo(deflector) < 4))
            return super.deflect(deflection == ProjectileDeflection.REDIRECTED ? DEFLECTION : deflection, deflector, owner, fromAttack);
        explode();
        return false;
    }

    @Override
    protected void onDeflected(@Nullable Entity deflector, boolean fromAttack) {
        playSound(OmniCrossbow.END_CRYSTAL_HIT, 1f, 1f);
    }

    private void explode() {
        if (getWorld().isClient) return;
        this.getWorld().createExplosion(this, getX(), getY(), getZ(), getVelocity().length() > 0.5 ? 4f : 6F, true, World.ExplosionSourceType.MOB);
        discard();
    }
}
