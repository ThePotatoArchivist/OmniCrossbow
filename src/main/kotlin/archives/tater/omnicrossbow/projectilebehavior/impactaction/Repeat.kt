package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult

@JvmRecord
data class Repeat(val times: IntProvider, val action: ImpactAction) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val count = times.sample(projectile.random)
        if (count <= 0) return false
        repeat(count - 1) {
            action.tryImpact(level, projectile, hit, originalItem)
        }
        return action.tryImpact(level, projectile, hit, originalItem)
    }

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    companion object {
        val CODEC: MapCodec<Repeat> = RecordCodecBuilder.mapCodec { it.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("times").forGetter(Repeat::times),
            ImpactAction.CODEC.fieldOf("action").forGetter(Repeat::action)
        ).apply(it, ::Repeat) }
    }
}