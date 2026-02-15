package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.network.addMovementClient
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnEntity
import archives.tater.omnicrossbow.registry.OmniCrossbowProjectileActions
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.NON_NEGATIVE_DOUBLE
import archives.tater.omnicrossbow.util.times
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.phys.Vec3
import java.util.*

@JvmRecord
data class ProjectileBehavior(
    val projectileAction: ProjectileAction,
    val velocityScale: Float,
    val cooldownTicks: Int,
    val shootSound: Optional<Holder<SoundEvent>>,
    val recoil: Optional<Recoil>,
    val ignoreGravityAiming: Boolean,
    val remainder: Either<Boolean, ItemStackTemplate>,
    val delay: Optional<Delay>,
) {
    constructor(
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        cooldownTicks: Int = 0,
        shootSound: Holder<SoundEvent>? = null,
        recoil: Recoil? = null,
        ignoreGravityAiming: Boolean = false,
        remainder: Boolean = false,
        delay: Delay? = null,
    ) : this(
        projectileAction,
        velocityScale,
        cooldownTicks,
        Optional.ofNullable(shootSound),
        Optional.ofNullable(recoil),
        ignoreGravityAiming,
        Either.left(remainder),
        Optional.ofNullable(delay)
    )

    private constructor(
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        cooldownTicks: Int = 0,
        shootSound: Holder<SoundEvent>? = null,
        recoil: Recoil? = null,
        ignoreGravityAiming: Boolean = false,
        remainder: ItemStackTemplate,
        delay: Delay? = null,
    ) : this(
        projectileAction,
        velocityScale,
        cooldownTicks,
        Optional.ofNullable(shootSound),
        Optional.ofNullable(recoil),
        ignoreGravityAiming,
        Either.right(remainder),
        Optional.ofNullable(delay)
    )

    fun getRemainder(projectile: ItemStack): ItemStack? = remainder.map(
        { if (it) when (val item = projectile.item) {
            is MobBucketItem -> item.content.bucket.defaultInstance
            else -> projectile.craftingRemainder?.create()
        } else null },
        { it.create() }
    )

    @JvmRecord
    data class Delay(
        val ticks: IntProvider,
        val chargeSound: Optional<Holder<SoundEvent>>,
    ) {
        constructor(ticks: IntProvider, chargeSound: Holder<SoundEvent>? = null) : this(ticks, Optional.ofNullable(chargeSound))

        companion object {
            val CODEC: Codec<Delay> = RecordCodecBuilder.create { it.group(
                IntProvider.NON_NEGATIVE_CODEC.fieldOf("ticks").forGetter(Delay::ticks),
                SoundEvent.CODEC.optionalFieldOf("charge_sound").forGetter(Delay::chargeSound)
            ).apply(it, ::Delay) }
        }
    }

    @JvmRecord
    data class Recoil(
        val amount: Double,
        val resetFalling: Boolean = false
    ) {
        fun apply(entity: LivingEntity, projectileMovement: Vec3) {
            entity.addMovementClient(projectileMovement.normalize() * -amount, resetFalling)
        }

        companion object {
            val CODEC: Codec<Recoil> = RecordCodecBuilder.create { it.group(
                NON_NEGATIVE_DOUBLE.fieldOf("amount").forGetter(Recoil::amount),
                Codec.BOOL.optionalFieldOf("reset_falling", false).forGetter(Recoil::resetFalling)
            ).apply(it, ::Recoil) }

            val SHORT_CODEC: Codec<Recoil> = Codec.either(CODEC, NON_NEGATIVE_DOUBLE).xmap(
                { either -> either.map({ it }, ::Recoil) },
                { if (it.resetFalling) Either.left(it) else Either.right(it.amount) }
            )
        }
    }

    companion object {
        @JvmField
        val CODEC: MapCodec<ProjectileBehavior> = RecordCodecBuilder.mapCodec { it.group(
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("velocity_scale", 1f).forGetter(ProjectileBehavior::velocityScale),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cooldown_ticks", 0).forGetter(ProjectileBehavior::cooldownTicks),
            SoundEvent.CODEC.optionalFieldOf("shoot_sound").forGetter(ProjectileBehavior::shootSound),
            Recoil.SHORT_CODEC.optionalFieldOf("recoil").forGetter(ProjectileBehavior::recoil),
            Codec.BOOL.optionalFieldOf("ignore_gravity_aiming", false).forGetter(ProjectileBehavior::ignoreGravityAiming),
            Codec.either(Codec.BOOL, ItemStackTemplate.CODEC).optionalFieldOf("remainder", Either.left(false)).forGetter(ProjectileBehavior::remainder),
            Delay.CODEC.optionalFieldOf("delay").forGetter(ProjectileBehavior::delay),
        ).apply(it, ::ProjectileBehavior) }

        @JvmField
        val ITEM_FILTERED_CODEC = ItemFiltered.createCodec(CODEC)

        @JvmField
        val DEFAULT = ProjectileBehavior(ProjectileAction.Default)

        @JvmStatic
        fun of(
            projectileAction: ProjectileAction,
            velocityScale: Float = 1f,
            remainder: ItemStackTemplate? = null
        ) = if (remainder == null)
            ProjectileBehavior(projectileAction, velocityScale)
        else
            ProjectileBehavior(projectileAction, velocityScale, remainder = remainder)

        fun getFallback(item: Item): ProjectileBehavior = when (item) {
            is SpawnEggItem -> OmniCrossbowProjectileActions.FROM_ENTITY_DATA
            is MobBucketItem -> OmniCrossbowProjectileActions.FROM_BUCKET
            is BoatItem -> OmniCrossbowProjectileActions.SPAWN_BOAT
            is MinecartItem -> OmniCrossbowProjectileActions.SPAWN_MINECART
            is BlockItem if item.block is FallingBlock -> SpawnEntity.FallingBlock(item.block.defaultBlockState())
            else -> null
        }?.let {
            of(it, 0.3f, remainder = if (item is MobBucketItem) ItemStackTemplate(Items.WATER_BUCKET) else null)
        } ?: ProjectileBehavior(OmniCrossbowProjectileActions.CUSTOM_ITEM_PROJECTILE)

        @JvmStatic
        fun getBehavior(level: Level, projectile: ItemStack): ProjectileBehavior =
            ItemFiltered.getFirst(level.registryAccess(), OmniCrossbowRegistries.PROJECTILE_BEHAVIOR, projectile)
                ?: run { getFallback(projectile.item) }
    }
}
