package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.util.weightedRound
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult

class FreezingSnowball(type: EntityType<out Snowball>, level: Level) : Snowball(type, level) {
    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        hitResult.entity.ticksFrozen += hitResult.entity.ticksRequiredToFreeze
    }

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            val movement = deltaMovement
            val count = weightedRound(movement.length(), random)
            repeat(count) {
                level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    x - movement.x * it / count,
                    getY(0.5) - movement.y * it / count,
                    z - movement.z * it / count,
                    0.0, 0.0, 0.0
                )
            }
        }
    }
}