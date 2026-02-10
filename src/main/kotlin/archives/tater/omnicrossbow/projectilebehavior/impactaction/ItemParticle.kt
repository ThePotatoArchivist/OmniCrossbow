package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.util.NON_NEGATIVE_DOUBLE
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
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
        originalItem: ItemStack,
    ): Boolean {
        level.sendParticles(ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(originalItem)), hit.location.x, hit.location.y, hit.location.z, count, dx, dy, dz, speed)
        return true
    }

    companion object {
        val CODEC: MapCodec<ItemParticle> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(ItemParticle::count),
            NON_NEGATIVE_DOUBLE.fieldOf("dx").forGetter(ItemParticle::dx),
            NON_NEGATIVE_DOUBLE.fieldOf("dy").forGetter(ItemParticle::dy),
            NON_NEGATIVE_DOUBLE.fieldOf("dz").forGetter(ItemParticle::dz),
            NON_NEGATIVE_DOUBLE.fieldOf("speed").forGetter(ItemParticle::speed),
        ).apply(it, ::ItemParticle) }
    }
}