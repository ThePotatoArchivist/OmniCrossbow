package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.hasAny
import archives.tater.omnicrossbow.util.itemPredicateBuilder
import archives.tater.omnicrossbow.util.withComponents
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.SingleRegistryBootstrap
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches

object PredicateGenerator : SingleRegistryBootstrap<LootItemCondition> {
    val INTANGIBLE = ResourceKey.create(Registries.PREDICATE, OmniCrossbow.id("intangible"))

    override fun run(registry: BootstrapContext<LootItemCondition>) {
        registry.register(INTANGIBLE, toolMatches(itemPredicateBuilder {
            withComponents {
                hasAny(DataComponents.INTANGIBLE_PROJECTILE)
            }
        }).build())
    }
}