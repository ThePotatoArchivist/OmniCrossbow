package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnProjectile
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EntityType
import java.util.concurrent.CompletableFuture

class ProjectileBehaviorGenerator(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) : FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(
        registries: HolderLookup.Provider,
        entries: Entries
    ) {
        val items = entries.getLookup(Registries.ITEM)

        fun register(path: String, behavior: ProjectileBehavior) {
            entries.add(ResourceKey.create(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR, OmniCrossbow.id(path)), behavior)
        }

        register("egg", ProjectileBehavior(
            items.getOrThrow(ItemTags.EGGS),
            SpawnProjectile(EntityType.EGG)
        ))

        register("default", ProjectileBehavior(
            items.getOrThrow(ItemTags.ARROWS),
            ProjectileAction.Default
        ))
    }

    override fun getName(): String = "Projectile Behaviors"
}