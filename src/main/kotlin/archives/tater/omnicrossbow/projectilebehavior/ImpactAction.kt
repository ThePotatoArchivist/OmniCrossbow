package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import archives.tater.omnicrossbow.util.ContextKeySet
import archives.tater.omnicrossbow.util.validatedListCodec
import com.mojang.serialization.Codec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

fun interface ImpactAction<in T: HitResult> {
    fun tryImpact(level: ServerLevel, projectile: CustomItemProjectile, hit: T): Boolean

    typealias Series<T> = List<ConditionalEffect<ImpactAction<T>>>

    companion object {
        @JvmField
        val BLOCK_CODEC: Codec<ImpactAction<BlockHitResult>> = OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION.byNameCodec()
        @JvmField
        val ENTITY_CODEC: Codec<ImpactAction<EntityHitResult>> = OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION.byNameCodec()

        @JvmField
        val BLOCK_SERIES_CODEC: Codec<Series<BlockHitResult>> =
            validatedListCodec(ConditionalEffect.codec(BLOCK_CODEC), LootContextParamSets.HIT_BLOCK)
        @JvmField
        val ENTITY_SERIES_CODEC: Codec<Series<EntityHitResult>> =
            validatedListCodec(ConditionalEffect.codec(ENTITY_CODEC), LootContextParamSets.ENTITY_INTERACT)

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
    }
}