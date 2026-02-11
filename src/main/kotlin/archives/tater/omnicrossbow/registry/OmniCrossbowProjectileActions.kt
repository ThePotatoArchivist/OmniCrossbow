package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.mixin.behavior.access.BoatItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.MinecartItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.MobBucketItemAccessor
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.*
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.SpawnEggItem


object OmniCrossbowProjectileActions {
    private fun register(path: String, codec: MapCodec<out ProjectileAction.Inline>) {
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun <T: ProjectileAction> register(path: String, action: T): T =
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION, OmniCrossbow.id(path), action)

    private fun registerDelegated(path: String, action: Delegated) = register(path, action)

    private fun registerProjectile(path: String, action: SpawnProjectile<*>) = register(path, action)

    private fun <E: Entity> registerEntity(path: String, action: SpawnEntity<E>) = register(path, action)

    @JvmField
    val NONE = registerDelegated("none") { _, _, _, _, _, _ -> }

    @JvmField
    val CUSTOM_ITEM_PROJECTILE = registerProjectile("custom_item_projectile") { level, shooter, _, projectile ->
        CustomItemProjectile(shooter, level, projectile)
    }

    @JvmField
    val SPAWN_BOAT = registerEntity("spawn_entity/boat") {
        (it.item as? BoatItemAccessor)?.entityType
    }

    @JvmField
    val SPAWN_MINECART = registerEntity("spawn_entity/minecart") {
        (it.item as? MinecartItemAccessor)?.type
    }

    @JvmField
    val FROM_ENTITY_DATA = registerEntity("spawn_entity/from_entity_data") {
        SpawnEggItem.getType(it)
    }

    @JvmField
    val FROM_BUCKET = registerEntity("spawn_entity/from_bucket") {
        (it.item as? MobBucketItemAccessor)?.type
    }

    fun init() {
        register("default", ProjectileAction.Default)
        register("spawn_projectile", SpawnProjectile.Direct.CODEC)
        register("spawn_entity", SpawnEntity.Direct.CODEC)
        register("spawn_entity/falling_block", SpawnEntity.FallingBlock.CODEC)
        registerEntity("spawn_entity/item", SpawnEntity.Item)
        register("fire_beam", FireBeam.CODEC)
        register("pierce", Pierce.CODEC)
    }
}