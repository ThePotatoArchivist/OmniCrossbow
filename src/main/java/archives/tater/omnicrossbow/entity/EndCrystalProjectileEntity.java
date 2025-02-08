package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class EndCrystalProjectileEntity extends ExplosiveProjectileEntity {
    public EndCrystalProjectileEntity(EntityType<? extends EndCrystalProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EndCrystalProjectileEntity(World world, LivingEntity shooter, double directionX, double directionY, double directionZ) {
        super(OmniCrossbowEntities.END_CRYSTAL_PROJECTILE, shooter, 0, 0, 0, world);
        setVelocity(directionX, directionY, directionZ);
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
    protected float getDrag() {
        return 1;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
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

    private void explode() {
        if (getWorld().isClient) return;
        this.getWorld().createExplosion(this, getX(), getY(), getZ(), getVelocity().length() > 0.5 ? 4f : 6F, true, World.ExplosionSourceType.MOB);
        discard();
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        powerX = 0;
        powerY = 0;
        powerZ = 0;
    }
}
