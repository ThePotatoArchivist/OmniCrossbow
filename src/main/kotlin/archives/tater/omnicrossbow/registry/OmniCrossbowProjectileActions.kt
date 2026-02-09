package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.mixin.behavior.BoatItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.FireBlockInvoker
import archives.tater.omnicrossbow.mixin.behavior.MinecartItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.MobBucketItemAccessor
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.Delegated
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnEntity
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnProjectile
import archives.tater.omnicrossbow.util.*
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.FluidTags
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import java.util.stream.Stream


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

    @JvmField
    val FIRE_BEAM = registerDelegated("special/fire_beam") { pos, velocity, level, shooter, _, _ ->
        val direction = velocity.normalize()
        val end = pos + direction * 15.0
        var current = pos
        val burntPositions = Stream.builder<BlockPos>()
        while (true) {
            val hitResult = level.clip(ClipContext(
                current,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.WATER,
                shooter
            ))
            current = hitResult.location
            if (hitResult.type == HitResult.Type.MISS) break
            val blockPos = hitResult.blockPos
            val state = level[blockPos]
            if (!state.fluidState.isEmpty) {
                if (state.fluidState isIn FluidTags.WATER)
                    level.sendParticles(ParticleTypes.CLOUD, current.x, current.y + 0.1, current.z, 8, 0.0, 0.0, 0.0, 0.0)
                break
            }
            if ((Blocks.FIRE as FireBlockInvoker).invokeGetBurnOdds(state) > 0f) {
                level[blockPos] = Blocks.AIR.defaultBlockState()
                burntPositions.add(blockPos)
            } else {
                val endPos = blockPos.relative(hitResult.direction)
                if (level[endPos].canBeReplaced())
                    burntPositions.add(endPos)
                break
            }
        }
        for (blockPos in burntPositions.build()) {
            level[blockPos] = (Blocks.FIRE as FireBlockInvoker).invokeGetStateForPlacement(level, blockPos)
            level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                blockPos.x + 0.5,
                blockPos.y + 0.5,
                blockPos.z + 0.5,
                16,
                0.25,
                0.25,
                0.25,
                0.0
            )
        }
        for (entity in getEntitiesPierced(level, pos, current, 0.2, shooter)) {
            entity.hurtServer(level, level.damageSources().source(DamageTypes.IN_FIRE, shooter), 8f)
            entity.remainingFireTicks = 8 * 20
        }
        repeat((current - pos).length().toInt() * 4) {
            val particlePos = pos + direction * (it / 4.0)
            level.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 4, 0.0, 0.0, 0.0, 0.01)
        }
    }

    @JvmField
    val SONIC_BOOM = registerDelegated("sonic_boom") { pos, velocity, level, shooter, _, projectile ->
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
        register("default", ProjectileAction.Default)
        register("spawn_projectile", SpawnProjectile.Direct.CODEC)
        register("spawn_entity", SpawnEntity.Direct.CODEC)
        register("spawn_entity/falling_block", SpawnEntity.FallingBlock.CODEC)
        registerEntity("spawn_entity/item", SpawnEntity.Item)
    }
}