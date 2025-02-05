package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

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

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (isRemoved()) return true;
        var entity = source.getSource();
        if (entity != null && !source.isIn(DamageTypeTags.IS_EXPLOSION) && (!entity.isLiving() || squaredDistanceTo(entity) < 4)) {
            scheduleVelocityUpdate();
            if (getWorld().isClient) return true;
            addVelocity(entity instanceof ProjectileEntity ? entity.getVelocity().normalize() : entity.getRotationVector());
            setOwner(entity);
            playSound(OmniCrossbow.END_CRYSTAL_HIT, 1f, 1f);
        } else explode();

        return true;
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
