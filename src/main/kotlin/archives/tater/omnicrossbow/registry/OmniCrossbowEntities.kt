package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.*
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object OmniCrossbowEntities {
    private fun <T: Entity> register(
        key: ResourceKey<EntityType<*>>,
        factory: EntityType.EntityFactory<T>,
        category: MobCategory = MobCategory.MISC,
        init: EntityType.Builder<T>.() -> Unit
    ): EntityType<T> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.of(factory, category).apply(init).build(key)
    )

    private fun <T: Entity> register(id: Identifier, factory: EntityType.EntityFactory<T>, category: MobCategory = MobCategory.MISC, init: EntityType.Builder<T>.() -> Unit) =
        register(ResourceKey.create(Registries.ENTITY_TYPE, id), factory, category, init)

    private fun <T: Entity> register(path: String, factory: EntityType.EntityFactory<T>, category: MobCategory = MobCategory.MISC, init: EntityType.Builder<T>.() -> Unit) =
        register(OmniCrossbow.id(path), factory, category, init)

    private fun <T: Entity> registerProjectile(path: String, factory: EntityType.EntityFactory<T>, category: MobCategory = MobCategory.MISC, init: EntityType.Builder<T>.() -> Unit = {}) =
        register(path, factory, category) {
            noLootTable()
            sized(0.25f, 0.25f)
            clientTrackingRange(4)
            updateInterval(10)
            init()
        }

    @JvmField
    val DELEGATE_PROJECTILE = register("delegate_projectile", ::DelegateProjectile) {
        noSummon()
        noSave()
        noLootTable()
        sized(0f, 0f)
        clientTrackingRange(0)
    }

    @JvmField
    val CUSTOM_ITEM_PROJECTILE = registerProjectile("custom_item_projectile", ::CustomItemProjectile)

    @JvmField
    val SLIME_BALL = registerProjectile("slimeball", ::ThrownSlimeball)

    @JvmField
    val MAGMA_CREAM = registerProjectile("magma_cream", ::ThrownMagmaCream) {
        fireImmune()
    }

    @JvmField
    val FREEZING_SNOWBALL = registerProjectile("freezing_snowball", ::FreezingSnowball)

    @JvmField
    val END_CRYSTAL = registerProjectile("end_crystal", ::EndCrystalProjectile) {
        sized(1f, 1f)
        fireImmune()
    }

    @JvmField
    val EMBER = registerProjectile("ember", ::Ember) {
        sized(0.125f, 0.125f)
        fireImmune()
    }

    @JvmField
    val BEACON_LASER = register("beacon_laser", ::BeaconLaser) {
        sized(1f, 1f)
        noLootTable()
        fireImmune()
        clientTrackingRange(4)
        updateInterval(Int.MAX_VALUE)
        noSummon()
        noSave()
    }

    @JvmField
    val SPY_ENDER_EYE = register("spy_ender_eye", ::SpyEnderEye) {
        noLootTable()
        sized(0.325f, 0.325f)
        fireImmune()
        noSave()
    }

    @JvmField
    val GRAPPLE_FISHING_HOOK = registerProjectile("grapple_fishing_hook", ::GrappleFishingHook) {
        noSave()
        noSummon()
    }

    fun init() {

    }
}