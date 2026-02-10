package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.util.BLOCK_STATE_SHORT_CODEC
import archives.tater.omnicrossbow.util.set
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.HitResult

@JvmRecord
data class SetBlock(
    val state: BlockState
) : ImpactAction.Inline {

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        level[BlockPos.containing(hit.location)] = state
        return true
    }

    override val codec: MapCodec<out SetBlock> get() = CODEC

    companion object {
        val CODEC : MapCodec<SetBlock> = RecordCodecBuilder.mapCodec { it.group(
            BLOCK_STATE_SHORT_CODEC.fieldOf("block").forGetter(SetBlock::state)
        ).apply(it, ::SetBlock) }
    }
}