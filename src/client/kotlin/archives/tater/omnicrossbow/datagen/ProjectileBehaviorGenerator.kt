package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.condition.BreakingTimeProvider
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.impactaction.*
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnCustomProjectile
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnEntity
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnProjectile
import archives.tater.omnicrossbow.registry.*
import archives.tater.omnicrossbow.util.ItemPredicate
import archives.tater.omnicrossbow.util.hasAny
import archives.tater.omnicrossbow.util.withComponents
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.advancements.criterion.BlockPredicate
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.LocationPredicate.Builder.location
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.ConstantFloat
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.PointedDripstoneBlock
import net.minecraft.world.level.block.state.properties.DripstoneThickness
import net.minecraft.world.level.storage.loot.IntRange
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition.allOf
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition.anyOf
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert
import net.minecraft.world.level.storage.loot.predicates.LocationCheck.checkLocation
import net.minecraft.world.level.storage.loot.predicates.ValueCheckCondition.hasValue
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
        val blocks = entries.getLookup(Registries.BLOCK)

        fun register(path: String, behavior: ProjectileBehavior) {
            entries.add(ResourceKey.create(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR, OmniCrossbow.id(path)), behavior)
        }

        fun register(tag: TagKey<Item>, create: (ItemPredicate) -> ProjectileBehavior) {
            register(tag.location.path, create(ItemPredicate { of(items, tag) }))
        }

        fun register(item: ItemLike, create: (ItemPredicate) -> ProjectileBehavior) {
            register(BuiltInRegistries.ITEM.getKey(item.asItem()).path, create(ItemPredicate { of(items, item) }))
        }

        fun soundHolder(soundEvent: SoundEvent) = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)

        register(ItemTags.EGGS) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.EGG)) }
        register(Items.SNOWBALL) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.SNOWBALL)) }
        register(Items.ENDER_PEARL) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.ENDER_PEARL)) }
        register(Items.EXPERIENCE_BOTTLE) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.EXPERIENCE_BOTTLE)) }
        register(Items.SPLASH_POTION) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.SPLASH_POTION)) }
        register(Items.LINGERING_POTION) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.LINGERING_POTION)) }
        register(Items.FIRE_CHARGE) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.SMALL_FIREBALL), 0.03f, shootSound = soundHolder(SoundEvents.BLAZE_SHOOT)) }
        register(Items.WIND_CHARGE) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.WIND_CHARGE), 0.5f, shootSound = soundHolder(SoundEvents.WIND_CHARGE_THROW)) }
        register(Items.DRAGON_BREATH) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.DRAGON_FIREBALL), 0.03f, shootSound = soundHolder(SoundEvents.ENDER_DRAGON_SHOOT), remainder = true) }
        register(Items.WITHER_SKELETON_SKULL) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.WITHER_SKULL), 0.03f, shootSound = soundHolder(SoundEvents.WITHER_SHOOT)) }
        register(Items.TRIDENT) { ProjectileBehavior(it, SpawnProjectile.Direct(EntityType.TRIDENT), shootSound = SoundEvents.TRIDENT_THROW) }

        register(Items.ARMOR_STAND) { ProjectileBehavior(it, SpawnEntity.Direct(EntityType.ARMOR_STAND)) }
        register(Items.TNT) { ProjectileBehavior(it, SpawnEntity.Direct(EntityType.TNT)) }
        register(ItemTags.BOATS) { ProjectileBehavior(it, SpawnEntity.Boat, 0.5f) }

        register("spawn_eggs", ProjectileBehavior(ItemPredicate {
            withComponents {
                hasAny(DataComponents.ENTITY_DATA)
            }
        }, SpawnEntity.FromEgg, 0.5f))

        register("entity_buckets", ProjectileBehavior.of(ItemPredicate {
            withComponents {
                hasAny(DataComponents.BUCKET_ENTITY_DATA)
            }
        }, SpawnEntity.FromBucket, 0.5f, remainder = ItemStackTemplate(Items.WATER_BUCKET)))

        register(Items.FEATHER) { ProjectileBehavior(it, SpawnEntity.Item, velocityScale = 0.3f) }

        register(Items.POINTED_DRIPSTONE) { ProjectileBehavior(it, SpawnEntity.FallingBlock(Blocks.POINTED_DRIPSTONE.defaultBlockState()
            .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
            .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP_MERGE),
            damagePerDistance = 6f,
            damageMax = 40,
        ), 0.7f) }
        register(Items.ECHO_SHARD) { ProjectileBehavior(it, OmniCrossbowProjectileActions.SONIC_BOOM) }

        register(Items.GUNPOWDER) { ProjectileBehavior(it, SpawnCustomProjectile(
            AllOf(Explode(ConstantFloat.of(1f)), OmniCrossbowImpactActions.SHRINK),
        )) }

        register(OmniCrossbowTags.BUILTIN_PROJECTILES) { ProjectileBehavior(it, ProjectileAction.Default) }

        register(ConventionalItemTags.MINING_TOOL_TOOLS) { ProjectileBehavior(it, SpawnCustomProjectile(Conditional(
            condition = LootCondition(anyOf(
                OmniCrossbowConditions.TOOL_SUITABLE_FOR_BLOCK.builder,
                allOf(
                    invert(checkLocation(location().setBlock(BlockPredicate.Builder.block().of(blocks, OmniCrossbowTags.HAS_PREFERRED_TOOL)))),
                    hasValue(BreakingTimeProvider, IntRange.upperBound(20))
                )
            ).build()),
            onSuccess = OmniCrossbowImpactActions.BREAK_BLOCK,
        ))) }

        register("consumable", ProjectileBehavior(ItemPredicate {
            withComponents {
                hasAny(DataComponents.CONSUMABLE)
            }
        }, SpawnCustomProjectile(Conditional(
            condition = OmniCrossbowImpactActions.CONSUME_ITEM,
            onSuccess = ItemParticle(8, 0.0, 0.0, 0.0, 0.1)
        ))))

        register("equippable", ProjectileBehavior(ItemPredicate {
            withComponents {
                hasAny(DataComponents.EQUIPPABLE)
            }
        }, SpawnCustomProjectile(OmniCrossbowImpactActions.EQUIP)))
    }

    override fun getName(): String = "Projectile Behaviors"
}