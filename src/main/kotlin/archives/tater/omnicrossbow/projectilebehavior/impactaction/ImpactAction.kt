package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

interface ImpactAction {
    fun tryImpact(hit: EntityHitResult): Boolean
    fun tryImpact(hit: BlockHitResult): Boolean

    val codec: MapCodec<out ImpactAction>

    companion object {
        val CODEC: Codec<ImpactAction> = OmniCrossbowBuiltinRegistries.IMPACT_ACTION_TYPE
            .byNameCodec()
            .dispatch(ImpactAction::codec) { it }
    }
}