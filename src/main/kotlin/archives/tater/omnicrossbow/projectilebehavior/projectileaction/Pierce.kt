package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.network.ParticleBeamPayload
import archives.tater.omnicrossbow.network.addMovementClient
import archives.tater.omnicrossbow.util.*
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import java.util.*

@JvmRecord
data class Pierce(
    val distance: Double,
    val margin: Double,
    val particle: ParticleConfig,
    val particleStep: Double,
    val particleRandomness: Double = 1.0,
    val collideWithBlocks: Boolean = false,
    val knockback: Double = 0.0,
    val cheatOnGroundKnockback: Double = 1.0,
    val damage: Float = 0f,
    val damageType: Optional<Holder<DamageType>> = Optional.empty(),
) : Delegated, ProjectileAction.Inline {

    constructor(
        distance: Double,
        margin: Double,
        particle: ParticleConfig,
        particleStep: Double,
        particleRandomness: Double = 1.0,
        collideWithBlocks: Boolean = false,
        knockback: Double = 0.0,
        cheatOnGroundKnockback: Double = 1.0,
        damage: Float,
        damageType: Holder<DamageType>,
    ) : this(distance, margin, particle, particleStep, particleRandomness, collideWithBlocks, knockback, cheatOnGroundKnockback, damage, Optional.of(damageType))

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
        val stop = if (collideWithBlocks)
            level.clip(ClipContext(pos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).location
        else
            end
        for (entity in getEntitiesPierced(level, pos, stop, margin, shooter)) {
            if (damage > 0)
                entity.hurtServer(level, DamageSource(
                    damageType.orElse(
                        level.registryAccess().getOrThrow(DamageTypes.GENERIC)
                    ),
                    shooter
                ), damage)
            if (knockback > 0)
                entity.addMovementClient(direction.let {
                    if (cheatOnGroundKnockback == 1.0 || !entity.onGround() || it.y >= 0) it else it.multiply(1.0, cheatOnGroundKnockback, 1.0)
                } * knockback)
        }
        level.sendParticleBeam(ParticleBeamPayload(particle, pos, stop, particleStep, particleRandomness))
    }

    override val codec: MapCodec<out ProjectileAction.Inline> get() = CODEC

    companion object {
        val CODEC: MapCodec<Pierce> = RecordCodecBuilder.mapCodec { it.group(
            NON_NEGATIVE_DOUBLE.fieldOf("distance").forGetter(Pierce::distance),
            NON_NEGATIVE_DOUBLE.fieldOf("margin").forGetter(Pierce::margin),
            ParticleConfig.CODEC.fieldOf("particle").forGetter(Pierce::particle),
            NON_NEGATIVE_DOUBLE.fieldOf("particle_step").forGetter(Pierce::particleStep),
            Codec.doubleRange(0.0, 1.0).optionalFieldOf("particle_randomness", 1.0).forGetter(Pierce::particleRandomness),
            Codec.BOOL.optionalFieldOf("collide_with_blocks", false).forGetter(Pierce::collideWithBlocks),
            NON_NEGATIVE_DOUBLE.optionalFieldOf("knockback", 0.0).forGetter(Pierce::knockback),
            Codec.DOUBLE.optionalFieldOf("cheat_on_ground_knockback", 1.0).forGetter(Pierce::cheatOnGroundKnockback),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("damage", 0f).forGetter(Pierce::damage),
            DamageType.CODEC.optionalFieldOf("damage_type").forGetter(Pierce::damageType),
        ).apply(it, ::Pierce) }
    }
}