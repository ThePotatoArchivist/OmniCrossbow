package archives.tater.omnicrossbow.condition

import archives.tater.omnicrossbow.util.get
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.util.context.ContextKey
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootContextUser
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider

object BreakingTimeProvider : ContextFloatProvider, LootContextUser {
    val CODEC: MapCodec<BreakingTimeProvider> = MapCodec.unit(this)

    override fun getFloatUnsafe(context: LootContext): Float {
        val state = context.getOptional(LootContextParams.BLOCK_STATE) ?: return Float.MAX_VALUE
        val pos = context[LootContextParams.ORIGIN] ?: return Float.MAX_VALUE
        val blockSpeed = state.getDestroySpeed(context.level, BlockPos.containing(pos))
        if (blockSpeed < 0) return Float.MAX_VALUE
        val tool = context[LootContextParams.TOOL]?.get(DataComponents.TOOL)
        val toolSpeed = tool?.getMiningSpeed(state) ?: 1f
        val modifier = if (!state.requiresCorrectToolForDrops() || tool?.isCorrectForDrops(state) == true) 30 else 100
        return modifier * blockSpeed / toolSpeed
    }

    override fun getReferencedContextParams(): Set<ContextKey<*>> = [LootContextParams.BLOCK_STATE, LootContextParams.TOOL]

    override fun codec(): MapCodec<out ContextFloatProvider> = CODEC
}