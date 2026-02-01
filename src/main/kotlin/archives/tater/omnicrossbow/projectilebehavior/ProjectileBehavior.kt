package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnEntity
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.ITEM_PREDICATE_SHORT_CODEC
import archives.tater.omnicrossbow.util.ItemPredicate
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
) {
    constructor(items: ItemPredicate, projectileAction: ProjectileAction, velocityScale: Float = 1f, shootSound: Holder<SoundEvent>? = null)
            : this(items, projectileAction, velocityScale, Optional.ofNullable(shootSound))

    companion object {
        val CODEC: Codec<ProjectileBehavior> = RecordCodecBuilder.create { it.group(
            ITEM_PREDICATE_SHORT_CODEC.fieldOf("items").forGetter(ProjectileBehavior::items),
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction),
            Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("velocity_scale", 1f).forGetter(ProjectileBehavior::velocityScale),
            SoundEvent.CODEC.optionalFieldOf("shoot_sound").forGetter(ProjectileBehavior::shootSound),
        ).apply(it, ::ProjectileBehavior) }

        fun getFallback(item: Item): ProjectileBehavior = when (item) {
            is SpawnEggItem -> SpawnEntity.FromEgg
            is MobBucketItem -> SpawnEntity.FromBucket
            is BoatItem -> SpawnEntity.Boat
            is MinecartItem -> SpawnEntity.Minecart
            is BlockItem if item.block is FallingBlock -> SpawnEntity.FallingBlock(item.block.defaultBlockState())
            else -> null
        }?.let {
            ProjectileBehavior(ItemPredicate {}, it, 0.3f)
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
