package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnEntity
import archives.tater.omnicrossbow.registry.OmniCrossbowProjectileActions
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FallingBlock
import java.util.*

@JvmRecord
data class ProjectileBehavior(
    val projectileAction: ProjectileAction,
    val velocityScale: Float,
    val shootSound: Optional<Holder<SoundEvent>>,
    val remainder: Either<Boolean, ItemStackTemplate>,
) {
    constructor(
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        shootSound: Holder<SoundEvent>? = null,
        remainder: Boolean = false
    ) : this(projectileAction, velocityScale, Optional.ofNullable(shootSound), Either.left(remainder))

    private constructor(
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        shootSound: Holder<SoundEvent>? = null,
        remainder: ItemStackTemplate
    ) : this(projectileAction, velocityScale, Optional.ofNullable(shootSound), Either.right(remainder))

    fun getRemainder(projectile: ItemStack): ItemStack? = remainder.map(
        { if (it) when (val item = projectile.item) {
            is MobBucketItem -> item.content.bucket.defaultInstance
            else -> projectile.craftingRemainder?.create()
        } else null },
        { it.create() }
    )

    companion object {
        val CODEC: MapCodec<ProjectileBehavior> = RecordCodecBuilder.mapCodec { it.group(
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction),
            Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("velocity_scale", 1f).forGetter(ProjectileBehavior::velocityScale),
            SoundEvent.CODEC.optionalFieldOf("shoot_sound").forGetter(ProjectileBehavior::shootSound),
            Codec.either(Codec.BOOL, ItemStackTemplate.CODEC).optionalFieldOf("remainder", Either.left(false)).forGetter(ProjectileBehavior::remainder)
        ).apply(it, ::ProjectileBehavior) }

        val ITEM_FILTERED_CODEC = ItemFiltered.createCodec(CODEC)

        fun of(
            projectileAction: ProjectileAction,
            velocityScale: Float = 1f,
            shootSound: Holder<SoundEvent>? = null,
            remainder: ItemStackTemplate? = null
        ) = if (remainder == null)
            ProjectileBehavior(projectileAction, velocityScale, shootSound)
        else
            ProjectileBehavior(projectileAction, velocityScale, shootSound, remainder)

        fun getFallback(item: Item): ProjectileBehavior = when (item) {
            is SpawnEggItem -> OmniCrossbowProjectileActions.FROM_EGG
            is MobBucketItem -> OmniCrossbowProjectileActions.FROM_BUCKET
            is BoatItem -> OmniCrossbowProjectileActions.SPAWN_BOAT
            is MinecartItem -> OmniCrossbowProjectileActions.SPAWN_MINECART
            is BlockItem if item.block is FallingBlock -> SpawnEntity.FallingBlock(item.block.defaultBlockState())
            else -> null
        }?.let {
            of(it, 0.3f, remainder = if (item is MobBucketItem) ItemStackTemplate(Items.WATER_BUCKET) else null)
        } ?: ProjectileBehavior(OmniCrossbowProjectileActions.CUSTOM_ITEM_PROJECTILE) // TODO generic item projectile

        @JvmStatic
        fun getBehavior(level: Level, projectile: ItemStack): ProjectileBehavior =
            ItemFiltered.getFirst(level.registryAccess(), OmniCrossbowRegistries.PROJECTILE_BEHAVIOR, projectile)
                ?: run { getFallback(projectile.item) }
    }
}
