package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.access.FireBlockInvoker
import archives.tater.omnicrossbow.util.get
import archives.tater.omnicrossbow.util.set
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3

class Ember(type: EntityType<out Projectile>, level: Level) : Projectile(type, level) {

    private var age = 0

    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {}

    override fun getDefaultGravity(): Double = 0.03

    override fun tick() {
        super.tick()
        val hit = ProjectileUtil.getHitResultOnMoveVector(this, ::canHitEntity)
        hitTargetOrDeflectSelf(hit)
        updateRotation()
        applyGravity()

        if (!level().isClientSide && isInWater) {
            level().broadcastEntityEvent(this, EntityEvent.DEATH)
            playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.25f, 1f)
            discard()
            return
        }

        val movement = deltaMovement
        move(MoverType.SELF, movement)

        if (!level().isClientSide && age++ > MAX_AGE)
            discard()

        if (level().isClientSide && !onGround())
            level().addParticle(ParticleTypes.SMOKE, x, y, z, movement.x, movement.y, movement.z)
    }

    override fun handleEntityEvent(id: Byte) {
        if (id == EntityEvent.DEATH) {
            repeat(2) {
                level().addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.05, 0.0)
            }
            return
        }
        super.handleEntityEvent(id)
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val serverLevel = level() as? ServerLevel ?: return
        hitResult.entity.apply {
            remainingFireTicks += FIRE_TICKS
            hurtServer(serverLevel, damageSources().source(DamageTypes.IN_FIRE, this, getOwner()), DAMAGE)
        }
        discard()
    }

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        deltaMovement = Vec3.ZERO
        if (level().isClientSide || random.nextFloat() >= BLOCK_FIRE_CHANCE) return
        val firePos = hitResult.blockPos.relative(hitResult.direction)
        val oldState = level()[firePos]
        if (!oldState.canBeReplaced() || !oldState.fluidState.isEmpty) return
        val fireState = (Blocks.FIRE as FireBlockInvoker).invokeGetStateForPlacement(level(), firePos)
        level()[firePos] = fireState
        discard()
    }

    companion object {
        const val MAX_AGE = 20 * 30
        const val BLOCK_FIRE_CHANCE = 0.25f
        const val FIRE_TICKS = 4 * 20
        const val DAMAGE = 2f
    }
}