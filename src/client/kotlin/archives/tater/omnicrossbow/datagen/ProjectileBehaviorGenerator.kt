package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnEntity
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnProjectile
import archives.tater.omnicrossbow.registry.OmniCrossbowItemTags
import archives.tater.omnicrossbow.registry.OmniCrossbowProjectileActions
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
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

        fun register(tag: TagKey<Item>, create: (HolderSet<Item>) -> ProjectileBehavior) {
            register(tag.location.path, create(items.getOrThrow(tag)))
        }

        fun register(item: ItemLike, create: (HolderSet<Item>) -> ProjectileBehavior) {
            register(BuiltInRegistries.ITEM.getKey(item.asItem()).path, create(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item.asItem()))))
        }

        fun soundHolder(soundEvent: SoundEvent) = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)

        register(ItemTags.EGGS) { ProjectileBehavior(it, SpawnProjectile(EntityType.EGG)) }
        register(Items.SNOWBALL) { ProjectileBehavior(it, SpawnProjectile(EntityType.SNOWBALL)) }
        register(Items.ENDER_PEARL) { ProjectileBehavior(it, SpawnProjectile(EntityType.ENDER_PEARL)) }
        register(Items.EXPERIENCE_BOTTLE) { ProjectileBehavior(it, SpawnProjectile(EntityType.EXPERIENCE_BOTTLE)) }
        register(Items.SPLASH_POTION) { ProjectileBehavior(it, SpawnProjectile(EntityType.SPLASH_POTION)) }
        register(Items.LINGERING_POTION) { ProjectileBehavior(it, SpawnProjectile(EntityType.LINGERING_POTION)) }
        register(Items.FIRE_CHARGE) { ProjectileBehavior(it, SpawnProjectile(EntityType.SMALL_FIREBALL), 0.03f, shootSound = soundHolder(SoundEvents.BLAZE_SHOOT)) }
        register(Items.WIND_CHARGE) { ProjectileBehavior(it, SpawnProjectile(EntityType.WIND_CHARGE), 0.03f, shootSound = soundHolder(SoundEvents.WIND_CHARGE_THROW)) }
        register(Items.DRAGON_BREATH) { ProjectileBehavior(it, SpawnProjectile(EntityType.DRAGON_FIREBALL), 0.03f, shootSound = soundHolder(SoundEvents.ENDER_DRAGON_SHOOT)) }
        register(Items.WITHER_SKELETON_SKULL) { ProjectileBehavior(it, SpawnProjectile(EntityType.WITHER_SKULL), 0.03f, shootSound = soundHolder(SoundEvents.WITHER_SHOOT)) }
        register(Items.TRIDENT) { ProjectileBehavior(it, SpawnProjectile(EntityType.TRIDENT), shootSound = SoundEvents.TRIDENT_THROW) }

        register(Items.ARMOR_STAND) { ProjectileBehavior(it, SpawnEntity(EntityType.ARMOR_STAND)) }
        register(Items.TNT) { ProjectileBehavior(it, SpawnEntity(EntityType.TNT)) }
        register(Items.CHICKEN_SPAWN_EGG) { ProjectileBehavior(it, SpawnEntity(EntityType.CHICKEN)) }

        register(Items.ECHO_SHARD) { ProjectileBehavior(it, OmniCrossbowProjectileActions.SONIC_BOOM) }

        register(OmniCrossbowItemTags.BUILTIN_PROJECTILES) { ProjectileBehavior(it, ProjectileAction.Default) }
    }

    override fun getName(): String = "Projectile Behaviors"
}