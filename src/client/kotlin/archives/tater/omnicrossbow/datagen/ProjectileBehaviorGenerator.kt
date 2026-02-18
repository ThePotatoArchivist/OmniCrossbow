package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ItemFiltered
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior.Delay
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior.Recoil
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.*
import archives.tater.omnicrossbow.registry.*
import archives.tater.omnicrossbow.util.ItemPredicate
import archives.tater.omnicrossbow.util.ParticleConfig
import archives.tater.omnicrossbow.util.hasAny
import archives.tater.omnicrossbow.util.withComponents
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.PointedDripstoneBlock
import net.minecraft.world.level.block.state.properties.DripstoneThickness
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

        fun register(path: String, behavior: ItemFiltered<ProjectileBehavior>) {
            entries.add(ResourceKey.create(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR, OmniCrossbow.id(path)), behavior)
        }

        fun register(path: String, predicate: ItemPredicate, behavior: ProjectileBehavior) {
            register(path, ItemFiltered(predicate, behavior))
        }

        fun register(tag: TagKey<Item>, behavior: ProjectileBehavior) {
            register(tag.location.path, ItemPredicate { of(items, tag) }, behavior)
        }

        fun register(item: ItemLike, behavior: ProjectileBehavior) {
            register(BuiltInRegistries.ITEM.getKey(item.asItem()).path, ItemPredicate { of(items, item) }, behavior)
        }

        fun register(componentType: DataComponentType<*>, behavior: ProjectileBehavior) {
            register(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType)!!.path, ItemPredicate {
                withComponents {
                    hasAny(componentType)
                }
            }, behavior)
        }

        register(ItemTags.EGGS, ProjectileBehavior(SpawnProjectile.Direct(EntityType.EGG)))
        register(Items.ENDER_PEARL, ProjectileBehavior(SpawnProjectile.Direct(EntityType.ENDER_PEARL)))
        register(Items.EXPERIENCE_BOTTLE, ProjectileBehavior(SpawnProjectile.Direct(EntityType.EXPERIENCE_BOTTLE)))
        register(Items.SPLASH_POTION, ProjectileBehavior(SpawnProjectile.Direct(EntityType.SPLASH_POTION)))
        register(Items.LINGERING_POTION, ProjectileBehavior(SpawnProjectile.Direct(EntityType.LINGERING_POTION)))
        register(Items.FIRE_CHARGE, ProjectileBehavior(SpawnProjectile.Direct(EntityType.SMALL_FIREBALL), 0.03f, shootSound = soundHolder(SoundEvents.BLAZE_SHOOT), ignoreGravityAiming = true))
        register(Items.DRAGON_BREATH, ProjectileBehavior(SpawnProjectile.Direct(EntityType.DRAGON_FIREBALL), 0.03f, cooldownTicks = 2 * 20, shootSound = soundHolder(SoundEvents.ENDER_DRAGON_SHOOT), ignoreGravityAiming = true, remainder = true))
        register(Items.WITHER_SKELETON_SKULL, ProjectileBehavior(SpawnProjectile.Direct(EntityType.WITHER_SKULL), 0.03f, cooldownTicks = 2 * 20, shootSound = soundHolder(SoundEvents.WITHER_SHOOT), ignoreGravityAiming = true))
        register(Items.TRIDENT, ProjectileBehavior(SpawnProjectile.Direct(EntityType.TRIDENT), shootSound = SoundEvents.TRIDENT_THROW))

        register(Items.ARMOR_STAND, ProjectileBehavior(SpawnEntity.Direct(EntityType.ARMOR_STAND)))
        register(Items.TNT, ProjectileBehavior(SpawnEntity.Direct(EntityType.TNT)))
        register(ItemTags.BOATS, ProjectileBehavior(OmniCrossbowProjectileActions.SPAWN_BOAT, 0.5f))

        register(Items.SNOWBALL, ProjectileBehavior(SpawnProjectile.Direct(OmniCrossbowEntities.FREEZING_SNOWBALL)))
        register(Items.WIND_CHARGE, ProjectileBehavior(SpawnProjectile.CustomWindCharge(3f), 0.5f, shootSound = soundHolder(SoundEvents.BREEZE_SHOOT), ignoreGravityAiming = true))
        register(Items.SLIME_BALL, ProjectileBehavior(SpawnProjectile.Direct(OmniCrossbowEntities.SLIME_BALL)))
        register(Items.MAGMA_CREAM, ProjectileBehavior(SpawnProjectile.Direct(OmniCrossbowEntities.MAGMA_CREAM)))
        register(Items.END_CRYSTAL, ProjectileBehavior(SpawnProjectile.Direct(OmniCrossbowEntities.END_CRYSTAL), 0.1f, cooldownTicks = 4 * 20, ignoreGravityAiming = true))
        register(Items.ENDER_EYE, ProjectileBehavior(OmniCrossbowProjectileActions.SPY_ENDER_EYE, shootSound = soundHolder(SoundEvents.ENDER_EYE_LAUNCH), ignoreGravityAiming = true))

        register(Items.BLAZE_POWDER, ProjectileBehavior(
            ProjectileSpray(OmniCrossbowEntities.EMBER, ConstantInt.of(12), 32f),
            0.35f,
            cooldownTicks = 4 * 20,
            shootSound = soundHolder(SoundEvents.BLAZE_SHOOT),
            recoil = Recoil(0.5, resetFalling = true)
        ))

        register(Items.BREEZE_ROD, ProjectileBehavior(
            Pierce(
                16.0,
                2.0,
                ParticleConfig(ParticleTypes.GUST_EMITTER_SMALL),
                2.0,
                collideWithBlocks = true,
                cheatOnGroundKnockback = -0.5,
                knockback = 5.0,
            ),
            shootSound = SoundEvents.BREEZE_WIND_CHARGE_BURST,
            ignoreGravityAiming = true,
            recoil = Recoil(1.0, resetFalling = true),
        ))

        register(DataComponents.ENTITY_DATA, ProjectileBehavior(OmniCrossbowProjectileActions.FROM_ENTITY_DATA, 0.5f))

        register("entity_buckets", ItemPredicate {
            withComponents {
                hasAny(DataComponents.BUCKET_ENTITY_DATA)
            }
        }, ProjectileBehavior(OmniCrossbowProjectileActions.FROM_BUCKET, 0.5f, remainder = true))

        register(Items.FEATHER, ProjectileBehavior(SpawnEntity.Item, velocityScale = 0.3f))

        register(Items.POINTED_DRIPSTONE, ProjectileBehavior(SpawnEntity.FallingBlock(Blocks.POINTED_DRIPSTONE.defaultBlockState()
            .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
            .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP_MERGE),
            damagePerDistance = 6f,
            damageMax = 40,
        ), velocityScale = 0.7f))

        register(Items.ECHO_SHARD, ProjectileBehavior(
            Pierce(
                15.0,
                1.0,
                ParticleConfig(ParticleTypes.SONIC_BOOM),
                1.0,
                particleRandomness = 0.0,
                collideWithBlocks = false,
                knockback = 2.5,
                damage = 10f,
                damageType = registries.getOrThrow(OmniCrossbowDamageTypes.SONIC_BOOM)
            ),
            cooldownTicks = 6 * 20,
            shootSound = soundHolder(SoundEvents.WARDEN_SONIC_BOOM),
            ignoreGravityAiming = true,
            delay = Delay(ConstantInt.of(33), soundHolder(SoundEvents.WARDEN_SONIC_CHARGE)),
            recoil = Recoil(1.0)
        ))

        register(Items.NETHER_STAR, ProjectileBehavior(
            OmniCrossbowProjectileActions.BEACON_LASER,
            cooldownTicks = 8 * 20,
            shootSound = soundHolder(OmniCrossbowSounds.BEACON_FIRE),
            ignoreGravityAiming = true,
            delay = Delay(ConstantInt.of(14), soundHolder(OmniCrossbowSounds.BEACON_CHARGE))
        ))

        register(Items.BLAZE_ROD, ProjectileBehavior(FireBeam(
            distance = 15.0,
            margin = 0.2,
            damage = 8f,
            damageType = registries.getOrThrow(OmniCrossbowDamageTypes.FIRE_BEAM),
            fireTicks = 8 * 20,
            beamParticle = ParticleConfig(ParticleTypes.FLAME, 4, speed = 0.01),
            beamParticleStep = 0.25,
            beamParticleRandomness = 1.0,
            destroyParticle = ParticleConfig(ParticleTypes.LARGE_SMOKE, 16, 0.25, 0.25, 0.25),
            hitWaterParticle = ParticleConfig(ParticleTypes.CLOUD, 8),
        ), shootSound = soundHolder(SoundEvents.BLAZE_SHOOT), cooldownTicks = 2 * 20, ignoreGravityAiming = true))

        register(Items.FISHING_ROD, ProjectileBehavior(
            OmniCrossbowProjectileActions.GRAPPLE_FISHING_HOOK,
            shootSound = soundHolder(SoundEvents.FISHING_BOBBER_THROW),
            ignoreGravityAiming = true,
            keepProjectileLoaded = true,
        ))

        register(OmniCrossbowTags.BUILTIN_PROJECTILES, ProjectileBehavior(ProjectileAction.Default))

    }

    override fun getName(): String = "Projectile Behaviors"
}