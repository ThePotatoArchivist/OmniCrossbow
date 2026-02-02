package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.action.*
import archives.tater.omnicrossbow.projectilebehavior.action.Delegated
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object OmniCrossbowProjectileActions {
    private fun register(path: String, codec: MapCodec<out ProjectileAction>) {
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun registerSingleton(path: String, singleton: Singleton) {
        register(path, singleton.codec)
    }

    abstract class DelegatedSingleton : Singleton(), Delegated

    private fun registerSingleton(path: String, shoot: Delegated.ShootAction) = object : DelegatedSingleton() {
        override fun shoot(
            pos: Vec3,
            velocity: Vec3,
            level: ServerLevel,
            shooter: LivingEntity,
            weapon: ItemStack,
            projectile: ItemStack
        ) {
            shoot(pos, velocity, level, shooter, weapon, projectile)
        }
    }.also {
        register(path, it.codec)
    }

    private fun registerSpecial(path: String, shoot: Delegated.ShootAction) =
        registerSingleton("special/$path", shoot)

    @JvmField
    val NONE = registerSingleton("none") { _, _, _, _, _, _ -> }

    @JvmField
    val SONIC_BOOM = registerSpecial("sonic_boom") { pos, velocity, level, shooter, weapon, projectile ->
        val direction = velocity.normalize()
        val vec = direction * 15.0
        val end = pos + vec
        for (hit in ProjectileUtil.getManyEntityHitResult(
            level,
            shooter,
            pos,
            end,
            AABB(pos, end).inflate(1.0),
            { it is LivingEntity },
            1f,
            ClipContext.Block.COLLIDER,
            false
        )) with (hit.entity) {
            hurtServer(level, level.damageSources().sonicBoom(shooter), 10f)
            push(direction * 2.5)
        }
        repeat(15) {
            val particlePos = pos + direction * it.toDouble()
            level.sendParticles(ParticleTypes.SONIC_BOOM, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0)
        }
        level.playSound(null, shooter, SoundEvents.WARDEN_SONIC_BOOM, shooter.soundSource, 1f, 1f)
    }

    fun init() {
        register("spawn_projectile", SpawnProjectile.Direct.CODEC)
        register("spawn_entity", SpawnEntity.Direct.CODEC)
        register("spawn_entity/falling_block", SpawnEntity.FallingBlock.CODEC)
        registerSingleton("spawn_entity/from_egg", SpawnEntity.FromEgg)
        registerSingleton("spawn_entity/from_bucket", SpawnEntity.FromBucket)
        registerSingleton("spawn_entity/boat", SpawnEntity.Boat)
        registerSingleton("spawn_entity/minecart", SpawnEntity.Minecart)
        registerSingleton("spawn_entity/item", SpawnEntity.Item)
        registerSingleton("default", ProjectileAction.Default)
    }
}