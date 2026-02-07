package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

class AnyOf<T: HitResult> private constructor(val actions: List<ImpactAction<T>>) : ImpactAction<T> {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: T,
    ): Boolean = actions.any { it.tryImpact(level, projectile, hit) }

    companion object : CompositeType<AnyOf<BlockHitResult>, AnyOf<EntityHitResult>>() {
        fun <T: HitResult> createCodec(actionCodec: Codec<ImpactAction<T>>): MapCodec<AnyOf<T>> =
            actionCodec.listOf().fieldOf("actions").xmap(::AnyOf, AnyOf<T>::actions)

        override val blockCodec = createCodec(ImpactAction.BLOCK_CODEC)
        override val entityCodec = createCodec(ImpactAction.ENTITY_CODEC)

        fun block(vararg actions: ImpactAction<BlockHitResult>) = BlockInstance(AnyOf(actions.toList()))
        fun entity(vararg actions: ImpactAction<EntityHitResult>) = EntityInstance(AnyOf(actions.toList()))
    }
}