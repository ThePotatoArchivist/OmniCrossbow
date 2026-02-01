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
import net.minecraft.core.Holder
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

        fun register(tag: TagKey<Item>, action: ProjectileAction, shootSound: Holder<SoundEvent>? = null) {
            register(tag.location.path, ProjectileBehavior(items.getOrThrow(tag), action, shootSound))
        }

        fun register(tag: TagKey<Item>, action: ProjectileAction, shootSound: SoundEvent) {
            register(tag, action, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(shootSound))
        }

        fun register(item: Item, action: ProjectileAction, shootSound: Holder<SoundEvent>? = null) {
            register(BuiltInRegistries.ITEM.getKey(item).path, ProjectileBehavior(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(item)), action, shootSound))
        }

        fun register(item: Item, action: ProjectileAction, shootSound: SoundEvent) {
            register(item, action, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(shootSound))
        }

        register(ItemTags.EGGS, SpawnProjectile(EntityType.EGG))
        register(Items.SNOWBALL, SpawnProjectile(EntityType.SNOWBALL))
        register(Items.ENDER_PEARL, SpawnProjectile(EntityType.ENDER_PEARL))
        register(Items.EXPERIENCE_BOTTLE, SpawnProjectile(EntityType.EXPERIENCE_BOTTLE))
        register(Items.SPLASH_POTION, SpawnProjectile(EntityType.SPLASH_POTION))
        register(Items.LINGERING_POTION, SpawnProjectile(EntityType.LINGERING_POTION))
        register(Items.FIRE_CHARGE, SpawnProjectile(EntityType.SMALL_FIREBALL), SoundEvents.BLAZE_SHOOT)
        register(Items.WIND_CHARGE, SpawnProjectile(EntityType.WIND_CHARGE), SoundEvents.WIND_CHARGE_THROW)
        register(Items.DRAGON_BREATH, SpawnProjectile(EntityType.DRAGON_FIREBALL), SoundEvents.ENDER_DRAGON_SHOOT)
        register(Items.TRIDENT, SpawnProjectile(EntityType.TRIDENT), SoundEvents.TRIDENT_THROW)
        register(Items.WITHER_SKELETON_SKULL, SpawnProjectile(EntityType.WITHER_SKULL), SoundEvents.WITHER_SHOOT)

        register(Items.ARMOR_STAND, SpawnEntity(EntityType.ARMOR_STAND))
        register(Items.TNT, SpawnEntity(EntityType.TNT))
        register(Items.CHICKEN_SPAWN_EGG, SpawnEntity(EntityType.CHICKEN))

        register(Items.ECHO_SHARD, OmniCrossbowProjectileActions.SONIC_BOOM)

        register(OmniCrossbowItemTags.BUILTIN_PROJECTILES, ProjectileAction.Default)
    }

    override fun getName(): String = "Projectile Behaviors"
}