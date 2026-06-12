package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.mixin.behavior.access.AbstractArrowAccessor
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.util.contains
import archives.tater.omnicrossbow.util.merge
import archives.tater.omnicrossbow.util.set
import com.mojang.logging.LogUtils
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.arrow.AbstractArrow
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.ItemStack
import org.slf4j.Logger
import java.util.*

fun interface SpawnProjectile<T: Projectile> : ProjectileAction {
    fun createProjectile(
        level: ServerLevel,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack,
    ): T?

    @ConsistentCopyVisibility
    @JvmRecord
    data class Direct private constructor(
        val type: EntityType<*>,
        val nbt: Optional<CompoundTag> = Optional.empty()
    ) : SpawnProjectile<Projectile>, ProjectileAction.Inline {

        override fun createProjectile(
            level: ServerLevel,
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
            nbt.ifPresent {
                merge(LOGGER, it)
            }
        }

        override val codec: MapCodec<out Direct> get() = CODEC

        companion object {
            val CODEC: MapCodec<Direct> = RecordCodecBuilder.mapCodec { it.group(
                EntityType.CODEC.fieldOf("entity").forGetter(Direct::type),
                CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(Direct::nbt),
            ).apply(it, ::Direct) }

            private val LOGGER: Logger = LogUtils.getLogger();

            fun of(type: EntityType<out Projectile>, nbt: CompoundTag? = null) = Direct(type, Optional.ofNullable(nbt))

            operator fun invoke(type: EntityType<out Projectile>, nbt: CompoundTag? = null) = of(type, nbt)

            operator fun invoke(type: Holder<EntityType<out Projectile>>, nbt: CompoundTag? = null) = of(type.value(), nbt)
        }
    }

    @JvmRecord
    data class CustomWindCharge(val explosionRadius: Float) : SpawnProjectile<WindCharge>, ProjectileAction.Inline {
        override fun createProjectile(
            level: ServerLevel,
            shooter: LivingEntity,
            weapon: ItemStack,
            projectile: ItemStack
        ): WindCharge = WindCharge(EntityTypes.WIND_CHARGE, level).apply {
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