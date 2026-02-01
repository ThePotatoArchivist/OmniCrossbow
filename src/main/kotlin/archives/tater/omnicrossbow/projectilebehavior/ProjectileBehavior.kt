package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnEntity
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.ITEM_PREDICATE_SHORT_CODEC
import archives.tater.omnicrossbow.util.ItemPredicate
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.*
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FallingBlock
import java.util.*

@JvmRecord
data class ProjectileBehavior(
    val items: ItemPredicate,
    val projectileAction: ProjectileAction,
    val velocityScale: Float,
    val shootSound: Optional<Holder<SoundEvent>>,
    val remainder: Either<Boolean, ItemStackTemplate>,
) {
    constructor(
        items: ItemPredicate,
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        shootSound: Holder<SoundEvent>? = null,
        remainder: Boolean = false
    )
        : this(items, projectileAction, velocityScale, Optional.ofNullable(shootSound), Either.left(remainder))

    private constructor(
        items: ItemPredicate,
        projectileAction: ProjectileAction,
        velocityScale: Float = 1f,
        shootSound: Holder<SoundEvent>? = null,
        remainder: ItemStackTemplate
    )
        : this(items, projectileAction, velocityScale, Optional.ofNullable(shootSound), Either.right(remainder))

    fun getRemainder(projectile: ItemStack): ItemStack = remainder.map(
        { if (it) projectile.craftingRemainder?.create() else null },
        { it.create() }
    )

    companion object {
        val CODEC: Codec<ProjectileBehavior> = RecordCodecBuilder.create { it.group(
            ITEM_PREDICATE_SHORT_CODEC.fieldOf("items").forGetter(ProjectileBehavior::items),
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction),
            Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("velocity_scale", 1f).forGetter(ProjectileBehavior::velocityScale),
            SoundEvent.CODEC.optionalFieldOf("shoot_sound").forGetter(ProjectileBehavior::shootSound),
            Codec.either(Codec.BOOL, ItemStackTemplate.CODEC).optionalFieldOf("remainder", Either.left(false)).forGetter(ProjectileBehavior::remainder)
        ).apply(it, ::ProjectileBehavior) }

        fun of(
            items: ItemPredicate,
            projectileAction: ProjectileAction,
            velocityScale: Float = 1f,
            shootSound: Holder<SoundEvent>? = null,
            remainder: ItemStackTemplate? = null
        ) = if (remainder == null)
            ProjectileBehavior(items, projectileAction, velocityScale, shootSound)
        else
            ProjectileBehavior(items, projectileAction, velocityScale, shootSound, remainder)

        fun getFallback(item: Item): ProjectileBehavior = when (item) {
            is SpawnEggItem -> SpawnEntity.FromEgg
            is MobBucketItem -> SpawnEntity.FromBucket
            is BoatItem -> SpawnEntity.Boat
            is MinecartItem -> SpawnEntity.Minecart
            is BlockItem if item.block is FallingBlock -> SpawnEntity.FallingBlock(item.block.defaultBlockState())
            else -> null
        }?.let {
            of(ItemPredicate {}, it, 0.3f, remainder = if (item is MobBucketItem) ItemStackTemplate(Items.WATER_BUCKET) else null)
        } ?: ProjectileBehavior(ItemPredicate {}, ProjectileAction.Default) // TODO generic item projectile

        @JvmStatic
        fun getBehavior(level: Level, projectile: ItemStack): ProjectileBehavior? =
            level.registryAccess().lookupOrThrow<ProjectileBehavior>(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR)
                .stream()
                .filter { it.items.test(projectile) }
                .findFirst()
                .orElseGet { getFallback(projectile.item) }
    }
}
