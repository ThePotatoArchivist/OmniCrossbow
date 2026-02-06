package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

abstract class CompositeType<BlockAction: ImpactAction<BlockHitResult>, EntityAction: ImpactAction<EntityHitResult>> {

    protected abstract val blockCodec: MapCodec<BlockAction>
    protected abstract val entityCodec: MapCodec<EntityAction>

    val blockInstanceCodec: MapCodec<CompositeType<BlockAction, *>.BlockInstance> = blockCodec.xmap(::BlockInstance, CompositeType<BlockAction, *>.BlockInstance::action)
    val entityInstanceCodec: MapCodec<CompositeType<*, EntityAction>.EntityInstance> = entityCodec.xmap(::EntityInstance, CompositeType<*, EntityAction>.EntityInstance::action)

    inner class BlockInstance(val action: BlockAction) : ImpactAction.Inline<BlockHitResult> {
        override val codec: MapCodec<out ImpactAction.Inline<BlockHitResult>>
            get() = blockInstanceCodec

        override fun tryImpact(
            level: ServerLevel,
            projectile: CustomItemProjectile,
            hit: BlockHitResult,
            context: LootContext
        ): Boolean = action.tryImpact(level, projectile, hit, context)
    }

    inner class EntityInstance(val action: EntityAction) : ImpactAction.Inline<EntityHitResult> {
        override val codec: MapCodec<out ImpactAction.Inline<EntityHitResult>>
            get() = entityInstanceCodec

        override fun tryImpact(
            level: ServerLevel,
            projectile: CustomItemProjectile,
            hit: EntityHitResult,
            context: LootContext
        ): Boolean = action.tryImpact(level, projectile, hit, context)
    }
}