package archives.tater.omnicrossbow.entity

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EntityType
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

    private fun shoot() {
        val level = level() as? ServerLevel ?: return
        val itemType = item.item as? CrossbowItem ?: return
        val player = createFakePlayer(level, this)
        itemType.performShooting(level, player, InteractionHand.MAIN_HAND, item, 3.15f, 1f, null)
        spawnAtLocation(level, item)
        discard()
    }

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        setPos(hitResult.location.relative(hitResult.direction, 1.0 / 16))
        val axis = hitResult.direction.axis
        deltaMovement = deltaMovement.with(axis, -deltaMovement[axis])
        shoot()
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        shoot()
    }

}