package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.util.ContextKeySet
import archives.tater.omnicrossbow.util.LootContext
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.Validatable
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class CheckLootCondition(
    val condition: LootItemCondition
) : ImpactAction.Inline {

    constructor(builder: LootItemCondition.Builder) : this(builder.build())

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack,
    ): Boolean = condition.test(LootContext(level, CUSTOM_PROJECTILE_CONTEXT) {
        withParameter(LootContextParams.ORIGIN, if (hit is BlockHitResult) hit.blockPos.center else hit.location)
        withOptionalParameter(LootContextParams.ATTACKING_ENTITY, projectile.owner)
        withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, projectile)
        withParameter(LootContextParams.TOOL, originalItem)
        when (hit) {
            is BlockHitResult -> withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(hit.blockPos))
            is EntityHitResult -> withParameter(LootContextParams.TARGET_ENTITY, hit.entity)
        }
    })

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    companion object {

        @JvmField
        val CUSTOM_PROJECTILE_CONTEXT = ContextKeySet {
            optional(LootContextParams.BLOCK_STATE)
            optional(LootContextParams.TARGET_ENTITY)
            required(LootContextParams.ORIGIN)
            optional(LootContextParams.ATTACKING_ENTITY)
            required(LootContextParams.DIRECT_ATTACKING_ENTITY)
            required(LootContextParams.TOOL)
        }

        val CODEC: MapCodec<CheckLootCondition> = RecordCodecBuilder.mapCodec { it.group(
            LootItemCondition.DIRECT_CODEC.validate(Validatable.validatorForContext(CUSTOM_PROJECTILE_CONTEXT)).fieldOf("condition").forGetter(CheckLootCondition::condition)
        ).apply(it, ::CheckLootCondition) }
    }
}