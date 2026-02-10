package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.mixin.behavior.FireBlockInvoker
import archives.tater.omnicrossbow.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.FluidTags
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import java.util.stream.Stream

@JvmRecord
data class FireBeam(
    val distance: Double,
    val margin: Double = 0.2,
    val damage: Float,
    val damageType: Holder<DamageType>,
    val fireTicks: Int,
    val beamParticle: ParticleOptions = ParticleTypes.FLAME,
    val destroyParticle: ParticleOptions = ParticleTypes.LARGE_SMOKE,
    val hitWaterParticle: ParticleOptions = ParticleTypes.CLOUD,
) : Delegated, ProjectileAction.Inline {
    override fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: ServerLevel,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) {
        val direction = velocity.normalize()
        val end = pos + direction * distance
        var current = pos
        val burntPositions = Stream.builder<BlockPos>()
        while (true) {
            val hitResult = level.clip(
                ClipContext(
                    current,
                    end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.WATER,
                    shooter
                )
            )
            current = hitResult.location
            if (hitResult.type == HitResult.Type.MISS) break
            val blockPos = hitResult.blockPos
            val state = level[blockPos]
            if (!state.fluidState.isEmpty) {
                if (state.fluidState isIn FluidTags.WATER)
                    level.sendParticles(hitWaterParticle, current.x, current.y + 0.1, current.z, 8, 0.0, 0.0, 0.0, 0.0)
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
                destroyParticle,
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
        for (entity in getEntitiesPierced(level, pos, current, margin, shooter)) {
            entity.hurtServer(level, DamageSource(damageType, shooter), damage)
            entity.remainingFireTicks += fireTicks
        }
        repeat((current - pos).length().toInt() * 4) {
            val particlePos = pos + direction * (it / 4.0)
            level.sendParticles(beamParticle, particlePos.x, particlePos.y, particlePos.z, 4, 0.0, 0.0, 0.0, 0.01)
        }
    }

    override val codec: MapCodec<out FireBeam> get() = CODEC
    
    companion object {
        val CODEC: MapCodec<FireBeam> = RecordCodecBuilder.mapCodec { it.group(
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("distance").forGetter(FireBeam::distance),
            Codec.doubleRange(0.0, Double.MAX_VALUE).fieldOf("margin").forGetter(FireBeam::margin),
            ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("damage").forGetter(FireBeam::damage),
            DamageType.CODEC.fieldOf("damage_type").forGetter(FireBeam::damageType),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("fire_ticks").forGetter(FireBeam::fireTicks),
            PARTICLE_OPTIONS_SHORT_CODEC.fieldOf("beam_particle").forGetter(FireBeam::beamParticle),
            PARTICLE_OPTIONS_SHORT_CODEC.fieldOf("destroy_particle").forGetter(FireBeam::destroyParticle),
            PARTICLE_OPTIONS_SHORT_CODEC.fieldOf("hit_water_particle").forGetter(FireBeam::hitWaterParticle),
        ).apply(it, ::FireBeam) }
    }
}