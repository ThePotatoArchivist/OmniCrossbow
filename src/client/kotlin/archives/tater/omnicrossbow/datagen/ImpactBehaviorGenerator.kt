package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.condition.BreakingTimeProvider
import archives.tater.omnicrossbow.condition.CanPickUpLoot
import archives.tater.omnicrossbow.projectilebehavior.ItemFiltered
import archives.tater.omnicrossbow.projectilebehavior.impactaction.*
import archives.tater.omnicrossbow.registry.OmniCrossbowConditions
import archives.tater.omnicrossbow.registry.OmniCrossbowImpactActions
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.EntityPredicate
import archives.tater.omnicrossbow.util.ItemPredicate
import archives.tater.omnicrossbow.util.hasAny
import archives.tater.omnicrossbow.util.withComponents
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.advancements.criterion.BlockPredicate
import net.minecraft.advancements.criterion.EntityTypePredicate
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.LocationPredicate.Builder.location
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.ConstantFloat
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.storage.loot.IntRange
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition.allOf
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition.anyOf
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert
import net.minecraft.world.level.storage.loot.predicates.LocationCheck.checkLocation
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition.hasValue
import java.util.concurrent.CompletableFuture

class ImpactBehaviorGenerator(output: FabricPackOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {

    override fun configure(
        registries: HolderLookup.Provider,
        entries: Entries
    ) {
        val items = entries.getLookup(Registries.ITEM)
        val blocks = entries.getLookup(Registries.BLOCK)
        val entities = entries.getLookup(Registries.ENTITY_TYPE)

        fun register(path: String, behavior: ItemFiltered<ImpactAction>) {
            entries.add(ResourceKey.create(OmniCrossbowRegistries.IMPACT_BEHAVIOR, OmniCrossbow.id(path)), behavior)
        }

        fun register(path: String, predicate: ItemPredicate, action: ImpactAction) {
            register(path, ItemFiltered(predicate, action))
        }

        fun register(tag: TagKey<Item>, behavior: ImpactAction) {
            register(tag.location.path, ItemPredicate { of(items, tag) }, behavior)
        }

        fun register(item: ItemLike, behavior: ImpactAction) {
            register(BuiltInRegistries.ITEM.getKey(item.asItem()).path, ItemPredicate { of(items, item) }, behavior)
        }

        register(Items.GUNPOWDER, AllOf(Explode(ConstantFloat.of(1f), fire = true), OmniCrossbowImpactActions.SHRINK))

        register("mining_tools", ItemPredicate { of(items, ConventionalItemTags.MINING_TOOL_TOOLS) }, Conditional(
            condition = LootCondition(
                anyOf(
                    OmniCrossbowConditions.TOOL_SUITABLE_FOR_BLOCK,
                    allOf(
                        invert(checkLocation(location().setBlock(
                            BlockPredicate.Builder.block().of(blocks, OmniCrossbowTags.HAS_PREFERRED_TOOL)
                        ))),
                        hasValue(BreakingTimeProvider, IntRange.upperBound(20))
                    )
                )
            ),
            onSuccess = AllOf(
                OmniCrossbowImpactActions.BREAK_BLOCK,
                OmniCrossbowImpactActions.DURABILITY_DAMAGE,
            )
        ))

        register("consumable", ItemPredicate {
            withComponents {
                hasAny(DataComponents.CONSUMABLE)
            }
        }, Conditional(
            condition = OmniCrossbowImpactActions.CONSUME_ITEM,
            onSuccess = ItemParticle(8, 0.0, 0.0, 0.0, 0.1)
        ))

        register("equippable", ItemPredicate {
            withComponents {
                hasAny(DataComponents.EQUIPPABLE)
            }
        }, Conditional(
            condition = LootCondition(
                anyOf(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.TARGET_ENTITY,
                        EntityPredicate {
                            entityType(EntityTypePredicate.of(entities, OmniCrossbowTags.CAN_ALWAYS_EQUIP))
                        }),
                    CanPickUpLoot(LootContext.EntityTarget.TARGET_ENTITY)
                )
            ),
            onSuccess = OmniCrossbowImpactActions.EQUIP
        ))

        register("use", ItemPredicate {}, OmniCrossbowImpactActions.USE_ITEM)
    }

    override fun getName(): String = "Impact Behaviors"
}