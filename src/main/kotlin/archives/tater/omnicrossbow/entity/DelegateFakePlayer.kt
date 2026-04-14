package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.mixin.behavior.access.EntityAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.LivingEntityInvoker
import archives.tater.omnicrossbow.util.getXRotForAngle
import archives.tater.omnicrossbow.util.getYRotForAngle
import net.fabricmc.fabric.api.entity.FakePlayer
import com.mojang.authlib.GameProfile
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

class DelegateFakePlayer(level: ServerLevel, val stack: ItemStack, val owner: LivingEntity?) : FakePlayer(level, createGameProfile(owner, stack)) {
    init {
        refreshDimensions()
    }

    override fun getDefaultDimensions(pose: Pose): EntityDimensions = EntityDimensions.fixed(0f, 0f)

    companion object {
        @JvmStatic
        tailrec fun getOriginalOwner(entity: Entity): Entity = if (entity is DelegateFakePlayer) {
            val owner = entity.owner
            if (owner == null) entity else getOriginalOwner(owner)
        } else
            entity

        private fun createGameProfile(owner: LivingEntity?, stack: ItemStack) = if (owner == null) {
            GameProfile(DEFAULT_UUID, "flying ${stack.hoverName.string}")
        } else {
            GameProfile(owner.uuid, "${stack.hoverName.string} shot by ${owner.displayName.string}")
        }

        @Suppress("CAST_NEVER_SUCCEEDS")
        fun create(level: ServerLevel, projectile: ThrowableItemProjectile, pos: Vec3, xRot: Float, yRot: Float) =
            DelegateFakePlayer(level, projectile.item, projectile.owner as? LivingEntity).apply {
                this as EntityAccessor
                this as LivingEntityInvoker

                snapTo(pos, yRot, xRot)
                yHeadRot = yRot
                yHeadRotO = yRot

                eyeHeight = 0f
                deltaMovement = projectile.deltaMovement
                setItemInHand(InteractionHand.MAIN_HAND, projectile.item)
                invokeCollectEquipmentChanges()
                setAttackStrengthTicker(Int.MAX_VALUE)
            }

        fun create(level: ServerLevel, projectile: ThrowableItemProjectile): DelegateFakePlayer {
            val angle = projectile.deltaMovement.toVector3f()
            return create(level, projectile, projectile.position(), getXRotForAngle(angle), getYRotForAngle(angle))
        }
    }
}
