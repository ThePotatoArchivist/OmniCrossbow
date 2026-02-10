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
import net.minecraft.util.valueproviders.UniformFloat
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks.COBWEB
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

        register(Items.POWDER_SNOW_BUCKET, OmniCrossbowImpactActions.USE_ITEM_ON_ENTITY_BASE)

        register(Items.COBWEB, AnyOf(
            Conditional(
                condition = AllOf(
                    OmniCrossbowImpactActions.IS_ENTITY,
                    CheckLootCondition(condition = OmniCrossbowConditions.REPLACEABLE_AT_ORIGIN),
                ),
                onSuccess = SideEffect(
                    main = SetBlock(COBWEB.defaultBlockState()),
                    secondary = AllOf(
                        OmniCrossbowImpactActions.SHRINK,
                        PlaySound(soundHolder(SoundEvents.COBWEB_PLACE))
                    )
                ),
            ),
            OmniCrossbowImpactActions.USE_ITEM_ON_ENTITY_BASE,
        ))

        register("glow", ItemPredicate {
            of(items, Items.GLOWSTONE_DUST, Items.GLOW_INK_SAC, Items.GLOW_BERRIES)
        }, SideEffect(
            main = ApplyEffects(MobEffectInstance(MobEffects.GLOWING, 10 * 20)),
            secondary = AllOf(
                itemParticle,
                PlaySound(soundHolder(SoundEvents.GLOW_INK_SAC_USE)),
                OmniCrossbowImpactActions.SHRINK,
            )
        ))

        register(Items.NOTE_BLOCK, SideEffect(
            main = Conditional(
                condition = OmniCrossbowImpactActions.IS_BLOCK,
                onSuccess = OmniCrossbowImpactActions.USE_ITEM,
                onFail = Damage()
            ),
            secondary = AllOf(List(5) {
                PlaySound(SoundEvents.NOTE_BLOCK_HARP, pitch = UniformFloat.of(0.5f, 1f))
            })
        ))

        register(Items.BELL, SideEffect(
            main = Conditional(
                condition = OmniCrossbowImpactActions.IS_BLOCK,
                onSuccess = OmniCrossbowImpactActions.USE_ITEM,
                onFail = Damage()
            ),
            secondary = PlaySound(soundHolder(SoundEvents.BELL_BLOCK))
        ))

        register(Items.AMETHYST_SHARD, SideEffect(
            main = AnyOf(
                OmniCrossbowImpactActions.IS_BLOCK,
                Damage(8f)
            ),
            secondary = AllOf(
                PlaySound(soundHolder(SoundEvents.AMETHYST_BLOCK_BREAK)),
                itemParticle,
                OmniCrossbowImpactActions.SHRINK
            )
        ))

        register(ItemTags.LIGHTNING_RODS, SideEffect(
            main = AnyOf(
                OmniCrossbowImpactActions.IS_ENTITY,
                AllOf(
                    notIntangible,
                    OmniCrossbowImpactActions.USE_ITEM,
                ),
            ),
            secondary = Conditional(
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
                )
            )
        ))

        register(ConventionalItemTags.BUCKETS, AnyOf(
            OmniCrossbowImpactActions.USE_ITEM,
            OmniCrossbowImpactActions.USE_BUCKET,
        ))

        register(DataComponents.FIREWORK_EXPLOSION, SideEffect(
            main = OmniCrossbowImpactActions.FIREWORK_EXPLOSION,
            secondary = OmniCrossbowImpactActions.SHRINK
        ))

        register(DataComponents.DYE, SideEffect(
            main = OmniCrossbowImpactActions.DYE,
            secondary = AllOf(
                itemParticle,
                PlaySound(soundHolder(SoundEvents.DYE_USE)),
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

        register(DataComponents.CONSUMABLE, SideEffect(
            main = Conditional(
                condition = OmniCrossbowImpactActions.IS_ENTITY,
                AnyOf(
                    OmniCrossbowImpactActions.USE_ITEM,
                    OmniCrossbowImpactActions.CONSUME_ITEM,
                ),
            ),
            secondary = Conditional(
                condition = CheckLootCondition(toolMatches(itemPredicateBuilder {
                    withComponents {
                        partial(OmniCrossbowConditions.CONSUMABLE_PREDICATE,
                            ConsumablePredicate(hasConsumeParticles = true)
                        )
                    }
                })),
                onSuccess = itemParticle,
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

        register(DataComponents.KINETIC_WEAPON, SideEffect(
            main = KineticDamage(),
            secondary = OmniCrossbowImpactActions.DURABILITY_DAMAGE
        ))

        register("use", ItemPredicate {}, AnyOf(
            ifNotIntangible(OmniCrossbowImpactActions.USE_ITEM),
            Damage()
        ))
    }

    override fun getName(): String = "Impact Behaviors"
}