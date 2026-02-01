package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.DelegateProjectile
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

    @JvmField
    val DELEGATE_PROJECTILE = register("delegate_projectile", ::DelegateProjectile) {
        noSummon()
        noSave()
        noLootTable()
        sized(0f, 0f)
        clientTrackingRange(0)
    }

    fun init() {

    }
}