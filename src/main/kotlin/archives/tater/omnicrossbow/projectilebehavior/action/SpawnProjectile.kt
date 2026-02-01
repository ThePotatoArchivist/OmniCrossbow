package archives.tater.omnicrossbow.projectilebehavior.action

import archives.tater.omnicrossbow.mixin.behavior.AbstractArrowAccessor
import com.mojang.serialization.MapCodec
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

@ConsistentCopyVisibility
@JvmRecord
data class SpawnProjectile private constructor(private val type: EntityType<*>) : ProjectileAction {
    fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack,
        isCrit: Boolean
    ): Projectile? = (type.create(level, EntitySpawnReason.DISPENSER) as? Projectile)?.apply {
        setPos(shooter.x, shooter.eyeY - 0.1, shooter.z)
        owner = shooter
        (this as? AbstractArrow)?.isCritArrow = isCrit
        (this as? AbstractArrowAccessor)?.setFiredFromWeapon(weapon.copy())
        (this as? ThrowableItemProjectile)?.item = projectile
    }

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnProjectile> = EntityType.CODEC.fieldOf("entity_type")
            .xmap(::SpawnProjectile, SpawnProjectile::type)

        fun of(type: EntityType<out Projectile>) = SpawnProjectile(type)

        operator fun invoke(type: EntityType<out Projectile>) = of(type)
    }
}