package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import archives.tater.omnicrossbow.util.get
import archives.tater.omnicrossbow.util.set
import archives.tater.omnicrossbow.util.weightedRound
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class ThrownMagmaCream(type: EntityType<out ThrownMagmaCream>, level: Level) : BouncingProjectile(type, level) {
    private var deflected = false

    override fun getDefaultItem(): Item = Items.MAGMA_CREAM

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            val movement = deltaMovement
            val count = weightedRound(movement.length(), random)
            repeat(count) {
                level().addParticle(
                    ParticleTypes.FLAME,
                    x - movement.x * it / count,
                    getY(0.5) - movement.y * it / count,
                    z - movement.z * it / count,
                    0.0, 0.0, 0.0
                )
            }
        }
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val level = level() as? ServerLevel ?: return
        if (!deflected && hitResult.entity == getOwner()) return
        with (hitResult.entity) {
            hurtServer(level, damageSources().source(OmniCrossbowDamageTypes.FIRE_PROJECTILE, this, getOwner()), 2f)
            remainingFireTicks += 4 * 20
        }
    }

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        val firePos = hitResult.blockPos.relative(hitResult.direction)
        if (level()[firePos].canBeReplaced())
            level()[firePos] = BaseFireBlock.getState(level(), firePos)
    }

    override fun onDeflection(byAttack: Boolean) {
        super.onDeflection(byAttack)
        deflected = true
    }

    override fun onStopBounce(hitResult: BlockHitResult) {
        level().broadcastEntityEvent(this, EntityEvent.DEATH)
    }
}