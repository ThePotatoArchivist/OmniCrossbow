package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EndCrystalProjectileEntity extends ThrownEntity {
    public EndCrystalProjectileEntity(EntityType<? extends EndCrystalProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EndCrystalProjectileEntity(double x, double y, double z, World world) {
        super(OmniCrossbowEntities.END_CRYSTAL_PROJECTILE, x, y, z, world);
    }

    public EndCrystalProjectileEntity(LivingEntity owner, World world) {
        super(OmniCrossbowEntities.END_CRYSTAL_PROJECTILE, owner, world);
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
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        explode();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        explode();
    }

    private static final ProjectileDeflection DEFLECTION = (projectile, hitEntity, random1) -> {
        projectile.addVelocity(hitEntity instanceof ProjectileEntity ? hitEntity.getVelocity().normalize() : hitEntity != null ? hitEntity.getRotationVector() : projectile.getVelocity().multiply(-1));
    };

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflector, @Nullable Entity owner, boolean fromAttack) {
        if (deflector == null || !deflector.isLiving() || squaredDistanceTo(deflector) < 4)
            return super.deflect(deflection == ProjectileDeflection.SIMPLE ? DEFLECTION : deflection, deflector, owner, fromAttack);
        explode();
        return false;
    }

    @Override
    protected void onDeflected(@Nullable Entity deflector, boolean fromAttack) {
        playSound(OmniCrossbow.END_CRYSTAL_HIT, 1f, 1f);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    private void explode() {
        if (getWorld().isClient) return;
        discard();
        DamageSource damageSource = getOwner() != null ? getDamageSources().explosion(this, getOwner()) : null;
        this.getWorld().createExplosion(this, damageSource, null, getX(), getY(), getZ(), getVelocity().length() > 0.5 ? 4f : 6F, true, World.ExplosionSourceType.MOB);
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }
}
