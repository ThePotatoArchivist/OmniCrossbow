package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.access.EntityAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.LivingEntityInvoker
import archives.tater.omnicrossbow.util.getXRotForAngle
import archives.tater.omnicrossbow.util.getYRotForAngle
import net.fabricmc.fabric.api.entity.FakePlayer
import com.mojang.authlib.GameProfile
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.phys.Vec3

fun createFakePlayer(
    level: ServerLevel,
    projectile: ThrowableItemProjectile,
    pos: Vec3,
    xRot: Float,
    yRot: Float,
): FakePlayer = FakePlayer.get(
    level,
    projectile.owner?.let { GameProfile(it.uuid, "${projectile.item.hoverName.string} shot by ${it.displayName?.string}") }
        ?: GameProfile(FakePlayer.DEFAULT_UUID, "flying ${projectile.item.hoverName.string}")
).apply {
    this as EntityAccessor
    this as LivingEntityInvoker
    snapTo(pos, yRot, xRot)
    eyeHeight = 0f
    deltaMovement = projectile.deltaMovement
    setItemInHand(InteractionHand.MAIN_HAND, projectile.item)
    invokeCollectEquipmentChanges()
    setAttackStrengthTicker(Int.MAX_VALUE)
}

fun createFakePlayer(
    level: ServerLevel,
    projectile: ThrowableItemProjectile
): FakePlayer {
    val angle = projectile.deltaMovement.toVector3f()
    return createFakePlayer(level, projectile, projectile.position(), getXRotForAngle(angle), getYRotForAngle(angle))
}