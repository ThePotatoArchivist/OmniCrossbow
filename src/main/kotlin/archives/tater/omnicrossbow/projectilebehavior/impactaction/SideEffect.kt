package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult

class SideEffect(
    val main: ImpactAction,
    val secondary: ImpactAction,
) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean = main.tryImpact(level, projectile, hit, originalItem).also {
        if (it) secondary.tryImpact(level, projectile, hit, originalItem)
    }

    override val codec: MapCodec<out SideEffect> get() = CODEC

    companion object {
        val CODEC: MapCodec<SideEffect> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.fieldOf("main").forGetter(SideEffect::main),
            ImpactAction.CODEC.fieldOf("secondary").forGetter(SideEffect::secondary),
        ).apply(it, ::SideEffect) }
    }
}