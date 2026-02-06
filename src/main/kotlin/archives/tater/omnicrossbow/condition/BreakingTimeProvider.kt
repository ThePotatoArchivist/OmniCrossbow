package archives.tater.omnicrossbow.condition

import archives.tater.omnicrossbow.util.get
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider

object BreakingTimeProvider : NumberProvider {
    val CODEC: MapCodec<BreakingTimeProvider> = MapCodec.unit(this)

    override fun getFloat(context: LootContext): Float {
        val state = context[LootContextParams.BLOCK_STATE]
        val blockSpeed = state.getDestroySpeed(context.level, BlockPos.containing(context[LootContextParams.ORIGIN]))
        if (blockSpeed < 0) return Float.MAX_VALUE
        val tool = context[LootContextParams.TOOL].get(DataComponents.TOOL)
        val toolSpeed = tool?.getMiningSpeed(state) ?: 1f
        val modifier = if (!state.requiresCorrectToolForDrops() || tool?.isCorrectForDrops(state) == true) 30 else 100
        return modifier * blockSpeed / toolSpeed
    }

    override fun codec(): MapCodec<out NumberProvider> = CODEC
}