package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import kotlin.math.ceil

@Suppress("UnstableApiUsage")
class BeaconLaser {
    var ticksRemaining: Int = DURATION // Server

    fun tickServer(level: ServerLevel, entity: LivingEntity) {
        ticksRemaining--

        val start = getStartPos(entity)
        for (target in getEntitiesPierced(entity.level(), start, start + entity.lookAngle * DISTANCE, 0.2, entity)) {
            target.hurtServer(
                level,
                entity.damageSources().source(OmniCrossbowDamageTypes.BEACON, entity),
                (((target as? LivingEntity)?.maxHealth ?: 20f) / DURATION)
                    .coerceAtLeast(1f)
                    .let { if (target isIn OmniCrossbowTags.UNCAPPED_BEACON_DAMAGE) it else it.coerceAtMost(MAX_DAMAGE / DURATION) }
                    .let { if (target isIn OmniCrossbowTags.EXTRA_BEACON_DAMAGE) 1.1f * it else it }
            )
        }

        if (ticksRemaining <= 0)
            entity.removeAttached(OmniCrossbowAttachments.BEACON_LASER)
    }

    class Display {
        var distance: Int = 0
        var animationTime: Int = 0
        var thickness: Float = 0f

        fun getAnimationTime(partialTickTime: Float) = animationTime + partialTickTime

        fun getThickness(expanding: Boolean, partialTickTime: Float) =
            ((thickness) +
                    partialTickTime * (if (expanding) TRANSITION_STEP else -TRANSITION_STEP))
                .coerceIn(0f, 1f)

        fun tickClient(entity: LivingEntity) {
            animationTime++
            val start = getStartPos(entity)
            val hit = entity.level().clip(ClipContext(
                start,
                start + entity.lookAngle * DISTANCE,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
            ))
            distance = ceil((hit.location - start).length()).toInt()
            if (OmniCrossbowAttachments.BEACON_LASER in entity) {
                if (thickness < 1f)
                    thickness = (thickness + 1f / TRANSITION_TICKS).coerceAtMost(1f)
            } else {
                if (thickness > 0f)
                    thickness = (thickness - 1f / TRANSITION_TICKS).coerceAtLeast(0f)
            }
        }
    }

    companion object {
        const val DISTANCE = 64.0
        const val DURATION = 40
        const val MAX_DAMAGE = 100f
        const val TRANSITION_TICKS = 5
        const val TRANSITION_STEP = 1f / TRANSITION_TICKS

        fun getStartPos(entity: LivingEntity): Vec3 = entity.eyePosition.subtract(0.0, 0.1, 0.0)

        @JvmStatic
        fun tickClient(entity: LivingEntity) {
            (if (OmniCrossbowAttachments.BEACON_LASER in entity)
                entity.getAttachedOrCreate(OmniCrossbowAttachments.BEACON_LASER_DISPLAY, ::Display)
            else
                entity[OmniCrossbowAttachments.BEACON_LASER_DISPLAY]
            )?.run {
                tickClient(entity)
                if (thickness <= 0)
                    entity.removeAttached(OmniCrossbowAttachments.BEACON_LASER_DISPLAY)
            }
        }
    }
}