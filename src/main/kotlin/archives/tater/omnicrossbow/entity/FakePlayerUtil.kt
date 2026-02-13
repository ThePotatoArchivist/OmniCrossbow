package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.access.EntityAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.LivingEntityInvoker
import archives.tater.omnicrossbow.util.ifNotNull
import archives.tater.omnicrossbow.util.orElse
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

fun createFakePlayer(
    level: ServerLevel,
    projectile: CustomItemProjectile,
    pos: Vec3 = projectile.position(),
    xRot: Float = projectile.xRot,
    yRot: Float = projectile.yRot,
): FakePlayer = (ifNotNull(projectile.owner as? Player) {
    FakePlayer.get(level, it.gameProfile) // TODO chamge username
} orElse {
    FakePlayer.get(level)
}).apply {
    this as EntityAccessor
    this as LivingEntityInvoker
    snapTo(pos, yRot, xRot)
    eyeHeight = 0f
    deltaMovement = projectile.deltaMovement
    setItemInHand(InteractionHand.MAIN_HAND, projectile.item)
    invokeCollectEquipmentChanges()
    setAttackStrengthTicker(Int.MAX_VALUE)
}