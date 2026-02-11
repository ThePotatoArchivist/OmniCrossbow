package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult

@JvmRecord
data class AllOf(override val actions: List<ImpactAction>) : Multiple {

    constructor(vararg actions: ImpactAction) : this(actions.toList())

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack,
    ): Boolean = actions.all { it.tryImpact(level, projectile, hit, originalItem) }

    override val codec: MapCodec<out AllOf> get() = CODEC

    companion object {
        val CODEC = Multiple.createCodec(::AllOf)
    }
}