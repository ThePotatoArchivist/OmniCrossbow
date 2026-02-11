package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.network.addMovementClient
import archives.tater.omnicrossbow.util.contains
import net.minecraft.core.component.DataComponents
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class ThrownSlimeball(type: EntityType<out ThrownSlimeball>, level: Level) : BouncingProjectile(type, level) {
    override fun getDefaultItem(): Item = Items.SLIME_BALL

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        if (level().isClientSide) return
        val entity = hitResult.entity
        val movement = deltaMovement
        entity.addMovementClient(movement.multiply(2.5, if (entity.onGround() && movement.y < 0) -0.5 else 1.5, 2.5))
        playSound(SoundEvents.SLIME_BLOCK_FALL)
        level().broadcastEntityEvent(this, EntityEvent.DEATH)
        discard()
    }

    override fun onStopBounce(hitResult: BlockHitResult) {
        if (DataComponents.INTANGIBLE_PROJECTILE in item) return
        val pos = hitResult.location
        val itemEntity = ItemEntity(level(), pos.x, pos.y, pos.z, item)
        itemEntity.setDefaultPickUpDelay()
        itemEntity.setDeltaMovement(0.0, 0.0, 0.0)
        level().addFreshEntity(itemEntity)
    }
}