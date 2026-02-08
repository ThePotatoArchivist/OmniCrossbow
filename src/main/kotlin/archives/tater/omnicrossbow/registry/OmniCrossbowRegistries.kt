package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ItemFiltered
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object OmniCrossbowRegistries {
    private fun <T: Any> of(path: String): ResourceKey<Registry<T>> = ResourceKey.createRegistryKey<T>(OmniCrossbow.id(path))

    // Builtin

    @JvmField val PROJECTILE_ACTION = of<ProjectileAction>("projectile_action")

    @JvmField val PROJECTILE_ACTION_TYPE = of<MapCodec<out ProjectileAction.Inline>>("projectile_action_type")

    @JvmField val IMPACT_ACTION = of<ImpactAction>("impact_action")

    @JvmField val IMPACT_ACTION_TYPE = of<MapCodec<out ImpactAction.Inline>>("impact_action_type")

    // Dynamic

    @JvmField val PROJECTILE_BEHAVIOR = of<ItemFiltered<ProjectileBehavior>>("projectile_behavior")

    @JvmField val IMPACT_BEHAVIOR = of<ItemFiltered<ImpactAction>>("impact_behavior")

    fun init() {
        DynamicRegistries.register(PROJECTILE_BEHAVIOR, ProjectileBehavior.ITEM_FILTERED_CODEC)
        DynamicRegistries.register(IMPACT_BEHAVIOR, ImpactAction.ITEM_FILTERED_CODEC)
    }
}