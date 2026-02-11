package archives.tater.omnicrossbow.entity

import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class ThrownMagmaCream(type: EntityType<out ThrownMagmaCream>, level: Level) : BouncingProjectile(type, level) {
    private var deflected = false

    override fun getDefaultItem(): Item = Items.MAGMA_CREAM

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            if (random.nextFloat() < 0.2f)
                level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0)
            if (random.nextFloat() < 0.2f)
                level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0)
        }
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val level = level() as? ServerLevel ?: return
        if (!deflected && hitResult.entity == getOwner()) return
        with (hitResult.entity) {
            hurtServer(level, damageSources().source(DamageTypes.IN_FIRE, this, getOwner()), 2f)
            remainingFireTicks += 4 * 20
        }
    }

    override fun onDeflection(byAttack: Boolean) {
        super.onDeflection(byAttack)
        deflected = true
    }

    override fun onStopBounce(hitResult: BlockHitResult) {
        level().broadcastEntityEvent(this, EntityEvent.DEATH)
    }
}