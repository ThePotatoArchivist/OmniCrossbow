package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.phys.HitResult

@JvmRecord
data class ItemParticle(
    val count: Int,
    val dx: Double,
    val dy: Double,
    val dz: Double,
    val speed: Double,
) : ImpactAction.Inline {
    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
    ): Boolean {
        level.sendParticles(ItemParticleOption(ParticleTypes.ITEM, projectile.item.item), hit.location.x, hit.location.y, hit.location.z, count, dx, dy, dz, speed)
        return true
    }

    companion object {
        private val NON_NEGATIVE_DOUBLE: Codec<Double> = Codec.doubleRange(0.0, Double.MAX_VALUE)

        val CODEC: MapCodec<ItemParticle> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(ItemParticle::count),
            NON_NEGATIVE_DOUBLE.fieldOf("dx").forGetter(ItemParticle::dx),
            Codec.DOUBLE.fieldOf("dy").forGetter(ItemParticle::dy),
            Codec.DOUBLE.fieldOf("dz").forGetter(ItemParticle::dz),
            Codec.DOUBLE.fieldOf("speed").forGetter(ItemParticle::speed),
        ).apply(it, ::ItemParticle) }
    }
}