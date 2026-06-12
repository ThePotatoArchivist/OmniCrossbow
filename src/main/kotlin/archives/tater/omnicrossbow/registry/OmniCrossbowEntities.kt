package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.entity.*
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
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
    ) = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        key,
        EntityType.Builder.of(factory, category).apply(init).build(key)
    )

    private fun <T: Entity> registerProjectile(key: ResourceKey<EntityType<*>>, factory: EntityType.EntityFactory<T>, category: MobCategory = MobCategory.MISC, init: EntityType.Builder<T>.() -> Unit = {}) =
        register(key, factory, category) {
            noLootTable()
            sized(0.25f, 0.25f)
            clientTrackingRange(4)
            updateInterval(10)
            init()
        }

    @JvmField
    val DELEGATE_PROJECTILE = register(OmniCrossbowEntityIds.DELEGATE_PROJECTILE, ::DelegateProjectile) {
        noSummon()
        noSave()
        noLootTable()
        sized(0f, 0f)
        clientTrackingRange(0)
    }

    @JvmField
    val CUSTOM_ITEM_PROJECTILE = registerProjectile(OmniCrossbowEntityIds.CUSTOM_ITEM_PROJECTILE, ::CustomItemProjectile)

    @JvmField
    val SLIME_BALL = registerProjectile(OmniCrossbowEntityIds.SLIME_BALL, ::ThrownSlimeball)

    @JvmField
    val MAGMA_CREAM = registerProjectile(OmniCrossbowEntityIds.MAGMA_CREAM, ::ThrownMagmaCream) {
        fireImmune()
    }

    @JvmField
    val FREEZING_SNOWBALL = registerProjectile(OmniCrossbowEntityIds.FREEZING_SNOWBALL, ::FreezingSnowball)

    @JvmField
    val CROSSBOW = registerProjectile(OmniCrossbowEntityIds.CROSSBOW, ::ThrownCrossbow)

    @JvmField
    val END_CRYSTAL = registerProjectile(OmniCrossbowEntityIds.END_CRYSTAL, ::EndCrystalProjectile) {
        sized(1f, 1f)
        fireImmune()
    }

    @JvmField
    val EMBER = registerProjectile(OmniCrossbowEntityIds.EMER, ::Ember) {
        sized(0.125f, 0.125f)
        fireImmune()
    }

    @JvmField
    val BEACON_LASER = register(OmniCrossbowEntityIds.BEACON_LASER, ::BeaconLaser) {
        sized(1f, 1f)
        noLootTable()
        fireImmune()
        clientTrackingRange(4)
        updateInterval(Int.MAX_VALUE)
        noSummon()
        noSave()
    }

    @JvmField
    val SPY_ENDER_EYE = register(OmniCrossbowEntityIds.SPY_ENDER_EYE, ::SpyEnderEye) {
        noLootTable()
        sized(0.5f, 0.5f)
        fireImmune()
        noSave()
    }

    @JvmField
    val GRAPPLE_FISHING_HOOK = registerProjectile(OmniCrossbowEntityIds.GRAPPLE_FISHING_HOOK, ::GrappleFishingHook) {
        noSave()
        noSummon()
    }

    fun init() {

    }
}