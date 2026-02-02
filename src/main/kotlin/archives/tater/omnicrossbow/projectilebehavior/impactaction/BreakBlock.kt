package archives.tater.omnicrossbow.projectilebehavior.impactaction

import com.mojang.serialization.MapCodec
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class BreakBlock : ImpactAction {
    override fun tryImpact(hit: EntityHitResult): Boolean = false

    override fun tryImpact(hit: BlockHitResult): Boolean {
        // TODO
        return true
    }

    override val codec: MapCodec<out ImpactAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<BreakBlock> = MapCodec.unit(BreakBlock())
    }
}