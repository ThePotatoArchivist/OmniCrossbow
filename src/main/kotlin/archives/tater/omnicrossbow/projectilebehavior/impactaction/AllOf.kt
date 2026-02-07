package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult

@JvmRecord
data class AllOf(val actions: List<ImpactAction>) : ImpactAction.Inline {

    constructor(vararg actions: ImpactAction) : this(actions.toList())

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack,
    ): Boolean = actions.all { it.tryImpact(level, projectile, hit, originalItem) }

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    companion object {
        val CODEC: MapCodec<AllOf> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.listOf().fieldOf("actions").forGetter(AllOf::actions)
        ).apply(it, ::AllOf) }
    }
}