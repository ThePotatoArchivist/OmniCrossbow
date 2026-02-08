package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class BlockOffset(
    val action: ImpactAction,
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0,
    val direction: Int = 0
) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean = action.tryImpact(
        level,
        projectile,
        (hit as? BlockHitResult)?.withPosition(hit.blockPos.offset(x, y, z).relative(hit.direction, direction)) ?: hit,
        originalItem
    )

    override val codec: MapCodec<out BlockOffset> get() = CODEC

    companion object {
        val CODEC: MapCodec<BlockOffset> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.fieldOf("action").forGetter(BlockOffset::action),
            Codec.INT.optionalFieldOf("x", 0).forGetter(BlockOffset::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(BlockOffset::y),
            Codec.INT.optionalFieldOf("z", 0).forGetter(BlockOffset::z),
            Codec.INT.optionalFieldOf("direction", 0).forGetter(BlockOffset::direction),
        ).apply(it, ::BlockOffset) }
    }
}