package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

object OmniCrossbowRegistries {
    private fun <T: Any> of(path: String): ResourceKey<Registry<T>> = ResourceKey.createRegistryKey<T>(OmniCrossbow.id(path))

    // Builtin

    @JvmField val PROJECTILE_ACTION_TYPE = of<MapCodec<out ProjectileAction>>("projectile_action_type")

    @JvmField val BLOCK_IMPACT_ACTION = of<ImpactAction<BlockHitResult>>("block_impact_action")

    @JvmField val BLOCK_IMPACT_ACTION_TYPE = of<MapCodec<out ImpactAction.Inline<BlockHitResult>>>("block_impact_action_type")

    @JvmField val ENTITY_IMPACT_ACTION = of<ImpactAction<EntityHitResult>>("entity_impact_action")

    @JvmField val ENTITY_IMPACT_ACTION_TYPE = of<MapCodec<out ImpactAction.Inline<EntityHitResult>>>("entity_impact_action_type")

    // Dynamic

    @JvmField val PROJECTILE_BEHAVIOR = of<ProjectileBehavior>("projectile_behavior")

    fun init() {
        DynamicRegistries.register(PROJECTILE_BEHAVIOR, ProjectileBehavior.CODEC)
    }
}