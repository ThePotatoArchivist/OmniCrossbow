package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

class Conditional<T: HitResult> private constructor(
    val condition: ImpactAction<T>,
    val onSuccess: ImpactAction<T>,
    val onFail: ImpactAction<T>
) : ImpactAction<T> {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: T,
    ): Boolean =
        if (condition.tryImpact(level, projectile, hit))
            onSuccess.tryImpact(level, projectile, hit)
        else
            onFail.tryImpact(level, projectile, hit)

    companion object : CompositeType<Conditional<BlockHitResult>, Conditional<EntityHitResult>>() {
        fun <T: HitResult> createCodec(actionCodec: Codec<ImpactAction<T>>): MapCodec<Conditional<T>> =
            RecordCodecBuilder.mapCodec { it.group(
                actionCodec.optionalFieldOf("condition", ImpactAction.None).forGetter(Conditional<T>::condition),
                actionCodec.optionalFieldOf("on_success", ImpactAction.None).forGetter(Conditional<T>::onSuccess),
                actionCodec.optionalFieldOf("on_fail", ImpactAction.None).forGetter(Conditional<T>::onFail),
            ).apply(it, ::Conditional) }

        override val blockCodec = createCodec(ImpactAction.BLOCK_CODEC)
        override val entityCodec = createCodec(ImpactAction.ENTITY_CODEC)

        fun block(
            condition: ImpactAction<BlockHitResult>,
            onSuccess: ImpactAction<BlockHitResult> = ImpactAction.None,
            onFail: ImpactAction<BlockHitResult> = ImpactAction.None,
        ) = BlockInstance(Conditional(condition, onSuccess, onFail))
        fun entity(
            condition: ImpactAction<EntityHitResult>,
            onSuccess: ImpactAction<EntityHitResult> = ImpactAction.None,
            onFail: ImpactAction<EntityHitResult> = ImpactAction.None,
        ) = EntityInstance(Conditional(condition, onSuccess, onFail))
    }
}