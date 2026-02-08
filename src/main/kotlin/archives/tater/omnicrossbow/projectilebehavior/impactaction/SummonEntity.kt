package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class SummonEntity(val entityType: EntityType<*>, val onTarget: Boolean = false) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean = entityType.spawn(
        level,
        when (hit) {
            is BlockHitResult if onTarget -> hit.blockPos
            is EntityHitResult if onTarget -> hit.entity.blockPosition()
            else -> BlockPos.containing(hit.location)
        },
        EntitySpawnReason.TRIGGERED
    )?.apply {
        (this as? LightningBolt)?.cause = projectile.owner as? ServerPlayer
        when (hit) {
            is BlockHitResult if onTarget -> {}
            is EntityHitResult if onTarget -> snapTo(hit.entity.position())
            else -> snapTo(hit.location)
        }
    } != null

    override val codec: MapCodec<out SummonEntity> get() = CODEC

    companion object {
        val CODEC: MapCodec<SummonEntity> = RecordCodecBuilder.mapCodec { it.group(
            EntityType.CODEC.fieldOf("entity").forGetter(SummonEntity::entityType),
            Codec.BOOL.optionalFieldOf("on_target", false).forGetter(SummonEntity::onTarget)
        ).apply(it, ::SummonEntity) }
    }
}