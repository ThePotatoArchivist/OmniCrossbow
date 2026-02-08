package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.EntityAccessor
import archives.tater.omnicrossbow.util.ifNotNull
import archives.tater.omnicrossbow.util.orElse
import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player

fun createFakePlayer(level: ServerLevel, projectile: CustomItemProjectile): FakePlayer = (ifNotNull(projectile.owner as? Player) {
    FakePlayer.get(level, it.gameProfile)
} orElse {
    FakePlayer.get(level)
}).apply {
    this as EntityAccessor
    snapTo(projectile.position(), projectile.xRot, projectile.yRot)
    eyeHeight = 0f
    deltaMovement = projectile.deltaMovement
    setItemInHand(InteractionHand.MAIN_HAND, projectile.item)
}