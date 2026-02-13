package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.mixin.behavior.access.AbstractArrowAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.ThrownTridentAccessor
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.util.contains
import archives.tater.omnicrossbow.util.set
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.arrow.ThrownTrident
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

fun interface SpawnProjectile<T: Projectile> : ProjectileAction {
    fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack,
    ): T?

    @ConsistentCopyVisibility
    @JvmRecord
    data class Direct private constructor(val type: EntityType<*>) : SpawnProjectile<Projectile>, ProjectileAction.Inline {

        override fun createProjectile(
            level: Level,
            shooter: LivingEntity,
            weapon: ItemStack,
            projectile: ItemStack
        ): Projectile? = (type.create(level, EntitySpawnReason.SPAWN_ITEM_USE) as? Projectile)?.apply {
            setPos(shooter.x, shooter.eyeY - 0.1, shooter.z)
            owner = shooter
            (this as? ThrowableItemProjectile)?.item = projectile
            (this as? AbstractArrow)?.apply {
                this as AbstractArrowAccessor
                setFiredFromWeapon(weapon.copy())
                invokeSetPickupItemStack(projectile)
                applyComponentsFromItemStack(projectile)
                if (DataComponents.INTANGIBLE_PROJECTILE in projectile)
                    pickup = AbstractArrow.Pickup.CREATIVE_ONLY
            }
            (this as? ThrownTrident)?.apply {
                this as ThrownTridentAccessor
                entityData[ThrownTridentAccessor.getID_LOYALTY()] = invokeGetLoyaltyFromItem(projectile)
                entityData[ThrownTridentAccessor.getID_FOIL()] = projectile.hasFoil()
            }
        }

        override val codec: MapCodec<out Direct> get() = CODEC

        companion object {
            val CODEC: MapCodec<Direct> = RecordCodecBuilder.mapCodec { it.group(
                EntityType.CODEC.fieldOf("entity").forGetter(Direct::type)
            ).apply(it, ::Direct) }

            fun of(type: EntityType<out Projectile>) = Direct(type)

            operator fun invoke(type: EntityType<out Projectile>) = of(type)
        }
    }

    @JvmRecord
    data class CustomWindCharge(val explosionRadius: Float) : SpawnProjectile<WindCharge>, ProjectileAction.Inline {
        override fun createProjectile(
            level: Level,
            shooter: LivingEntity,
            weapon: ItemStack,
            projectile: ItemStack
        ): WindCharge = WindCharge(EntityType.WIND_CHARGE, level).apply {
            setPos(shooter.x, shooter.eyeY - 0.1, shooter.z)
            owner = shooter
            this[OmniCrossbowAttachments.WIND_CHARGE_EXPLOSION_RADIUS] = explosionRadius
        }

        override val codec: MapCodec<out ProjectileAction.Inline> get() = CODEC

        companion object {
            val CODEC: MapCodec<CustomWindCharge> = RecordCodecBuilder.mapCodec { it.group(
                ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("explosion_radius").forGetter(CustomWindCharge::explosionRadius),
            ).apply(it, ::CustomWindCharge) }
        }
    }
}