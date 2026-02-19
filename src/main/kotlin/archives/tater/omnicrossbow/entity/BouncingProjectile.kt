package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.access.ProjectileAccessor
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult


abstract class BouncingProjectile(type: EntityType<out BouncingProjectile>, level: Level) :
    ThrowableItemProjectile(type, level) {

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)

        val movement = deltaMovement

        if (!(movement.multiply(HORIZONTAL_BOUNCE_FACTOR, 1.0, HORIZONTAL_BOUNCE_FACTOR).length() > MIN_BOUNCE_SPEED)) {
            onStopBounce(hitResult)
            discard()
            return
        }

        val axis = hitResult.direction.axis
        val newMovement = movement.with(axis, -0.5 * movement[axis])
        deltaMovement = newMovement
        needsSync = true

        // velocity is processed after collisions, this makes it so that it starts at the hit result next time collision is checked
        this as ProjectileAccessor
        setPos(hitResult.location)
        setLeftOwner(true)
        playSound(SoundEvents.SLIME_BLOCK_FALL)
        level().broadcastEntityEvent(this, EntityEvent.DEATH)
    }

    protected open fun onStopBounce(hitResult: BlockHitResult) {}

    override fun handleEntityEvent(id: Byte) {
        if (id != EntityEvent.DEATH) {
            super.handleEntityEvent(id)
            return
        }

        val particle = ItemParticleOption(ParticleTypes.ITEM, item)

        repeat(6) {
            level().addParticle(
                particle,
                x,
                y,
                z,
                0.3 * random.nextDouble() - 0.15,
                0.3 * random.nextDouble() - 0.15,
                0.3 * random.nextDouble() - 0.15,
            )
        }
    }

    companion object {
        const val MIN_BOUNCE_SPEED = 0.2
        const val HORIZONTAL_BOUNCE_FACTOR = 0.2
    }
}