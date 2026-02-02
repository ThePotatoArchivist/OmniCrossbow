package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import archives.tater.omnicrossbow.util.validatedListCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

interface ImpactAction {
    fun tryImpact(hit: EntityHitResult): Boolean
    fun tryImpact(hit: BlockHitResult): Boolean

    val codec: MapCodec<out ImpactAction>

    typealias Series = List<ConditionalEffect<ImpactAction>>

    companion object {
        val CODEC: Codec<ImpactAction> = OmniCrossbowBuiltinRegistries.IMPACT_ACTION_TYPE
            .byNameCodec()
            .dispatch(ImpactAction::codec) { it }

        val SERIES_CODEC: Codec<Series> = validatedListCodec(ConditionalEffect.codec(CODEC), LootContextParamSets.HIT_BLOCK)
    }
}