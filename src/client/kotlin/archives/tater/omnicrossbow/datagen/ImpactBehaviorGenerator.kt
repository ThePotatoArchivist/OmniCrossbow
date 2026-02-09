package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.condition.BreakingTimeProvider
import archives.tater.omnicrossbow.condition.CanPickUpLoot
import archives.tater.omnicrossbow.condition.ConsumablePredicate
import archives.tater.omnicrossbow.projectilebehavior.ItemFiltered
import archives.tater.omnicrossbow.projectilebehavior.impactaction.*
import archives.tater.omnicrossbow.registry.OmniCrossbowConditions
import archives.tater.omnicrossbow.registry.OmniCrossbowImpactActions
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.*
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.advancements.criterion.BlockPredicate.Builder.block
import net.minecraft.advancements.criterion.EntityTypePredicate
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.LocationPredicate.Builder.location
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.ConstantFloat
import net.minecraft.world.entity.EntityType
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
import net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition.hasValue
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck.weather
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

        val itemParticle = ItemParticle(8, 0.0, 0.0, 0.0, 0.1)

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

        fun register(componentType: DataComponentType<*>, behavior: ImpactAction) {
            register(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType)!!.path, ItemPredicate {
                withComponents {
                    hasAny(componentType)
                }
            }, behavior)
        }

        val lootNotIntangible = invert(toolMatches(itemPredicateBuilder {
            withComponents {
                hasAny(DataComponents.INTANGIBLE_PROJECTILE)
            }
        }))

        val notIntangible = CheckLootCondition(lootNotIntangible)

        fun ifNotIntangible(action: ImpactAction) = Conditional(
                condition = notIntangible,
                onSuccess = action
            )

        register(Items.GUNPOWDER, AllOf(Explode(ConstantFloat.of(1f), fire = true), OmniCrossbowImpactActions.SHRINK))

        register(Items.MILK_BUCKET, OmniCrossbowImpactActions.CONSUME_ITEM)

        register(ItemTags.LIGHTNING_RODS, Conditional(
            condition = Conditional(
                condition = OmniCrossbowImpactActions.IS_BLOCK,
                onSuccess = Conditional(
                    condition = notIntangible,
                    onSuccess = OmniCrossbowImpactActions.USE_ITEM,
                ),
                onFail = OmniCrossbowImpactActions.PASS
            ),
            onSuccess = Conditional(
                condition = BlockOffset(
                    CheckLootCondition(allOf(
                        checkLocation(location().setCanSeeSky(true)),
                        weather().setThundering(true)
                    )),
                    direction = 1
                ),
                onSuccess = AllOf(
                    BlockOffset(
                        SummonEntity(EntityType.LIGHTNING_BOLT, onTarget = true),
                        direction = 1,
                        y = 1
                    ),
                    PlaySound(SoundEvents.TRIDENT_THUNDER)
                ),
                onFail = OmniCrossbowImpactActions.PASS
            )
        ))

        register(ConventionalItemTags.BUCKETS, AnyOf(
            OmniCrossbowImpactActions.USE_ITEM,
            OmniCrossbowImpactActions.USE_BUCKET,
        ))

        register(DataComponents.FIREWORK_EXPLOSION, Conditional(
            condition = OmniCrossbowImpactActions.FIREWORK_EXPLOSION,
            onSuccess = OmniCrossbowImpactActions.SHRINK
        ))

        register(DataComponents.DYE, Conditional(
            condition = OmniCrossbowImpactActions.DYE,
            onSuccess = AllOf(
                itemParticle,
                OmniCrossbowImpactActions.SHRINK
            )
        ))

        register(DataComponents.TOOL, Conditional(
            condition = CheckLootCondition(
                anyOf(
                    OmniCrossbowConditions.TOOL_SUITABLE_FOR_BLOCK,
                    allOf(
                        invert(checkLocation(location().setBlock(
                            block().of(blocks, OmniCrossbowTags.HAS_PREFERRED_TOOL)
                        ))),
                        hasValue(BreakingTimeProvider, IntRange.upperBound(20))
                    ),
                    allOf(
                        checkLocation(location().setBlock(block().of(blocks, BlockTags.LEAVES))),
                        toolMatches(itemPredicateBuilder {
                            of(items, ConventionalItemTags.SHEAR_TOOLS)
                        })
                    )
                )
            ),
            onSuccess = AllOf(
                OmniCrossbowImpactActions.BREAK_BLOCK,
                OmniCrossbowImpactActions.DURABILITY_DAMAGE,
            )
        ))

        register(DataComponents.CONSUMABLE, Conditional(
            condition = Conditional(
                condition = OmniCrossbowImpactActions.IS_ENTITY,
                AnyOf(
                    OmniCrossbowImpactActions.USE_ITEM,
                    OmniCrossbowImpactActions.CONSUME_ITEM,
                ),
            ),
            onSuccess = Conditional(
                condition = CheckLootCondition(toolMatches(itemPredicateBuilder {
                    withComponents {
                        partial(OmniCrossbowConditions.CONSUMABLE_PREDICATE,
                            ConsumablePredicate(hasConsumeParticles = true)
                        )
                    }
                })),
                onSuccess = itemParticle,
                onFail = OmniCrossbowImpactActions.PASS
            )
        ))

        register(DataComponents.EQUIPPABLE, Conditional(
            condition = CheckLootCondition(allOf(
                lootNotIntangible,
                anyOf(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.TARGET_ENTITY,
                        EntityPredicate {
                            entityType(EntityTypePredicate.of(entities, OmniCrossbowTags.CAN_ALWAYS_EQUIP))
                        }
                    ),
                    CanPickUpLoot(LootContext.EntityTarget.TARGET_ENTITY)
                )
            )),
            onSuccess = OmniCrossbowImpactActions.EQUIP
        ))

        register("shears", ItemPredicate {
            of(items, ConventionalItemTags.SHEAR_TOOLS)
        }, Conditional(
            condition = OmniCrossbowImpactActions.HAIRCUT,
            onSuccess = AllOf(
                OmniCrossbowImpactActions.DURABILITY_DAMAGE,
                PlaySound(soundHolder(SoundEvents.SHEARS_SNIP)),
            )
        ))

        register(DataComponents.KINETIC_WEAPON, KineticDamage())

        register("use", ItemPredicate {}, AnyOf(
            ifNotIntangible(OmniCrossbowImpactActions.USE_ITEM),
            Damage()
        ))
    }

    override fun getName(): String = "Impact Behaviors"
}