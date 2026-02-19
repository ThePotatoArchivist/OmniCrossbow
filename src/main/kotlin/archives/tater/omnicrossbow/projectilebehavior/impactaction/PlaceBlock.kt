package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.entity.DelegateFakePlayer
import archives.tater.omnicrossbow.util.get
import archives.tater.omnicrossbow.util.set
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class PlaceBlock(
    val block: Block
) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val blockHit = when (hit) {
            is BlockHitResult -> hit
            is EntityHitResult -> BlockHitResult(hit.location, Direction.UP, hit.entity.blockPosition(), false)
            else -> return false
        }
        val player = DelegateFakePlayer.create(level, projectile)
        val context = BlockPlaceContext(player, InteractionHand.MAIN_HAND, projectile.item, blockHit)
        val placePos = context.clickedPos
        if (!level[placePos].canBeReplaced(context)) return false
        val state = block.getStateForPlacement(context) ?: return false
        if (!state.canSurvive(level, placePos)) return false
        level[placePos] = state
        return true
    }

    override val codec: MapCodec<out PlaceBlock> get() = CODEC

    companion object {
        val CODEC: MapCodec<PlaceBlock> = RecordCodecBuilder.mapCodec { it.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(PlaceBlock::block),
        ).apply(it, ::PlaceBlock) }
    }
}