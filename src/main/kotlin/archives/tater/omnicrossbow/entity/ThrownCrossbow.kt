package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class ThrownCrossbow(type: EntityType<out ThrownCrossbow>, level: Level) : ThrowableItemProjectile(type, level) {

//    private var player: FakePlayer? = null

    override fun getDefaultItem(): Item = Items.CROSSBOW

    private fun shoot(target: LivingEntity?) {
        val level = level() as? ServerLevel ?: return
        val itemType = item.item as? CrossbowItem ?: return
        val player = DelegateFakePlayer.create(level, this)
        itemType.performShooting(level, player, InteractionHand.MAIN_HAND, item, 3.15f, 1f, target)
        spawnAtLocation(level, item)
        discard()
    }

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        setPos(hitResult.location.relative(hitResult.direction, bbWidth / 2.0))
        val axis = hitResult.direction.axis
        deltaMovement = deltaMovement.with(axis, -deltaMovement[axis])
        shoot(null)
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        setPos(position() + deltaMovement * -HIT_ENTITY_BACKUP)
        shoot(hitResult.entity as? LivingEntity)
    }

    companion object {
        const val HIT_ENTITY_BACKUP = 1.0
    }
}