package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.HitResult

@JvmRecord
data class Conditional(
    val condition: ImpactAction,
    val onSuccess: ImpactAction = ImpactAction.None,
    val onFail: ImpactAction = ImpactAction.None,
) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
    ): Boolean =
        if (condition.tryImpact(level, projectile, hit))
            onSuccess.tryImpact(level, projectile, hit)
        else
            onFail.tryImpact(level, projectile, hit)

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    companion object {
        val CODEC: MapCodec<Conditional> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.fieldOf("condition").forGetter(Conditional::condition),
            ImpactAction.CODEC.optionalFieldOf("on_success", ImpactAction.None).forGetter(Conditional::onSuccess),
            ImpactAction.CODEC.optionalFieldOf("on_fail", ImpactAction.None).forGetter(Conditional::onFail),
        ).apply(it, ::Conditional) }
    }
}