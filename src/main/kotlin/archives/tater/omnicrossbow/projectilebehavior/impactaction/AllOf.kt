package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

class AllOf<T: HitResult> private constructor(val actions: List<ImpactAction<T>>) : ImpactAction<T> {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: T,
        context: LootContext
    ): Boolean = actions.all { it.tryImpact(level, projectile, hit, context) }

    companion object : CompositeType<AllOf<BlockHitResult>, AllOf<EntityHitResult>>() {
        fun <T: HitResult> createCodec(actionCodec: Codec<ImpactAction<T>>): MapCodec<AllOf<T>> =
            actionCodec.listOf().fieldOf("actions").xmap(::AllOf, AllOf<T>::actions)

        override val blockCodec = createCodec(ImpactAction.BLOCK_CODEC)
        override val entityCodec = createCodec(ImpactAction.ENTITY_CODEC)

        fun block(vararg actions: ImpactAction<BlockHitResult>) = BlockInstance(AllOf(actions.toList()))
        fun entity(vararg actions: ImpactAction<EntityHitResult>) = EntityInstance(AllOf(actions.toList()))
    }
}