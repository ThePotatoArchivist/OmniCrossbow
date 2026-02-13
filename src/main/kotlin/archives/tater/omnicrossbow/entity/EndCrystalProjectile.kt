package archives.tater.omnicrossbow.entity

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class EndCrystalProjectile(type: EntityType<out EndCrystalProjectile>, level: Level) :
    AbstractHurtingProjectile(type, level) {

    private var redirectedByAttack = false

    init {
        accelerationPower = 0.0
    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (level().isClientSide) return
        level().explode(this, x, y, z, if (redirectedByAttack) 4f else 6f, true, Level.ExplosionInteraction.BLOCK)
        discard()
    }

    override fun shouldBurn(): Boolean = false

    override fun getTrailParticle(): ParticleOptions? = null

    override fun getInertia(): Float = 1f

    override fun onDeflection(byAttack: Boolean) {
        if (byAttack) {
            playSound(SoundEvents.ANVIL_LAND, 0.5f, 1.2f)
            redirectedByAttack = true
        }
    }

    override fun tick() {
        super.tick()
        if (!level().isClientSide) return
        repeat(8) {
            level().addParticle(
                ParticleTypes.PORTAL,
                x,
                getY(0.5),
                z,
                1.2 * random.nextDouble() - 0.6,
                1.2 * random.nextDouble() - 0.6,
                1.2 * random.nextDouble() - 0.6,
            )
        }
    }
}