package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.util.ContextKeySet
import archives.tater.omnicrossbow.util.LootContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.level.storage.loot.Validatable
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

abstract class LootCondition(
    val condition: LootItemCondition
) : ImpactAction.Inline<HitResult> {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
    ): Boolean = condition.test(LootContext(level, when (hit) {
        is BlockHitResult -> BLOCK_CONTEXT
        is EntityHitResult -> ENTITY_CONTEXT
        else -> return false
    }) {
        withParameter(LootContextParams.ORIGIN, hit.location)
        withOptionalParameter(LootContextParams.ATTACKING_ENTITY, projectile.owner)
        withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, projectile)
        withParameter(LootContextParams.TOOL, projectile.item)
        when (hit) {
            is BlockHitResult -> withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(hit.blockPos))
            is EntityHitResult -> withParameter(LootContextParams.TARGET_ENTITY, hit.entity)
        }
    })

    class Block(condition: LootItemCondition) : LootCondition(condition) {
        override val codec: MapCodec<out ImpactAction.Inline<HitResult>> get() = BLOCK_CODEC
    }

    class Entity(condition: LootItemCondition) : LootCondition(condition) {
        override val codec: MapCodec<out ImpactAction.Inline<HitResult>> get() = ENTITY_CODEC
    }

    companion object {

        @JvmField
        val BLOCK_CONTEXT = ContextKeySet {
            required(LootContextParams.BLOCK_STATE)
            required(LootContextParams.ORIGIN)
            optional(LootContextParams.ATTACKING_ENTITY)
            required(LootContextParams.DIRECT_ATTACKING_ENTITY)
            required(LootContextParams.TOOL)
        }

        @JvmField
        val ENTITY_CONTEXT = ContextKeySet {
            required(LootContextParams.TARGET_ENTITY)
            required(LootContextParams.ORIGIN)
            optional(LootContextParams.ATTACKING_ENTITY)
            required(LootContextParams.DIRECT_ATTACKING_ENTITY)
            required(LootContextParams.TOOL)
        }

        fun createCodec(factory: (LootItemCondition) -> LootCondition, keys: ContextKeySet): MapCodec<LootCondition> = RecordCodecBuilder.mapCodec { it.group(
            LootItemCondition.DIRECT_CODEC.validate(Validatable.validatorForContext(keys)).fieldOf("condition").forGetter(LootCondition::condition)
        ).apply(it, factory) }

        val BLOCK_CODEC = createCodec(::Block, BLOCK_CONTEXT)
        val ENTITY_CODEC = createCodec(::Entity, ENTITY_CONTEXT)
    }
}