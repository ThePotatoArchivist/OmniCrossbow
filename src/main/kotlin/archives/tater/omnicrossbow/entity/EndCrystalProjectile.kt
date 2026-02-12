package archives.tater.omnicrossbow.entity

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class EndCrystalProjectile(type: EntityType<out EndCrystalProjectile>, level: Level) :
    AbstractHurtingProjectile(type, level) {

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        level().explode(this, x, y, z, 6f, true, Level.ExplosionInteraction.BLOCK)
        discard()
    }

    override fun shouldBurn(): Boolean = false

    override fun tick() {
        super.tick()
        if (!level().isClientSide) return
        repeat(8) {
            level().addParticle(
                ParticleTypes.PORTAL,
                x,
                getY(0.5),
                z,
                0.25 * random.nextGaussian(),
                0.25 * random.nextGaussian(),
                0.25 * random.nextGaussian(),
            )
        }
    }
}