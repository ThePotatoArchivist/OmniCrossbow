package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.contains
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.*

@JvmRecord
data class ProjectileBehavior(
    val items: HolderSet<Item>,
    val projectileAction: ProjectileAction,
    val velocityScale: Float,
    val shootSound: Optional<Holder<SoundEvent>>,
) {
    constructor(items: HolderSet<Item>, projectileAction: ProjectileAction, velocityScale: Float = 1f, shootSound: Holder<SoundEvent>? = null)
            : this(items, projectileAction, velocityScale, Optional.ofNullable(shootSound))

    companion object {
        val CODEC: Codec<ProjectileBehavior> = RecordCodecBuilder.create { it.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ProjectileBehavior::items),
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction),
            Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("velocity_scale", 1f).forGetter(ProjectileBehavior::velocityScale),
            SoundEvent.CODEC.optionalFieldOf("shoot_sound").forGetter(ProjectileBehavior::shootSound),
        ).apply(it, ::ProjectileBehavior) }

        @JvmStatic
        fun getBehavior(level: Level, projectile: ItemStack): ProjectileBehavior? =
            level.registryAccess().lookupOrThrow<ProjectileBehavior>(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR)
                .stream()
                .filter { projectile in it.items }
                .findFirst()
                .orElse(null)
    }
}
