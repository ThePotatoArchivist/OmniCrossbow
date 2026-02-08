package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.mixin.behavior.BoatItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.MinecartItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.MobBucketItemAccessor
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.Delegated
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnEntity
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnProjectile
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.AABB

object OmniCrossbowProjectileActions {
    private fun register(path: String, codec: MapCodec<out ProjectileAction.Inline>) {
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun register(path: String, action: ProjectileAction): ProjectileAction =
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION, OmniCrossbow.id(path), action)

    private fun registerDelegated(path: String, action: Delegated) = register(path, action)

    private fun registerProjectile(path: String, action: SpawnProjectile<*>) = register(path, action)

    private fun registerEntity(path: String, action: SpawnEntity<*>) = register(path, action)

    init {
        register("default", ProjectileAction.Default)
        register("spawn_projectile", SpawnProjectile.Direct.CODEC)
        register("spawn_entity", SpawnEntity.Direct.CODEC)
        register("spawn_entity/falling_block", SpawnEntity.FallingBlock.CODEC)
        registerEntity("spawn_entity/item", SpawnEntity.Item)
    }

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
    val FROM_EGG = registerEntity("spawn_entity/from_egg") {
        SpawnEggItem.getType(it)
    }

    @JvmField
    val FROM_BUCKET = registerEntity("spawn_entity/from_bucket") {
        (it.item as? MobBucketItemAccessor)?.type
    }

    @JvmField
    val SONIC_BOOM = registerDelegated("sonic_boom") { pos, velocity, level, shooter, weapon, projectile ->
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

    }
}