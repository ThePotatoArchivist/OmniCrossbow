package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.action.Delegated
import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.action.Singleton
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnEntity
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnProjectile
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object OmniCrossbowProjectileActions {
    private fun register(path: String, codec: MapCodec<out ProjectileAction>) {
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun registerSingleton(path: String, shoot: Delegated.ShootAction) = object : Singleton() {
        override fun shoot(
            pos: Vec3,
            velocity: Vec3,
            level: Level,
            shooter: LivingEntity,
            weapon: ItemStack,
            projectile: ItemStack,
        ) {
            shoot(pos, velocity, level, shooter, weapon, projectile)
        }
    }.also {
        register("special/$path", it.codec)
    }

    private fun registerSpecial(path: String, shoot: Delegated.ShootAction) {
        registerSingleton("special/$path", shoot)
    }

    val NONE = registerSingleton("none") { _, _, _, _, _, _ -> }

    fun init() {
        register("spawn_projectile", SpawnProjectile.CODEC)
        register("spawn_entity", SpawnEntity.CODEC)
        register("default", ProjectileAction.Default.codec)
    }
}