package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.narrow
import archives.tater.omnicrossbow.util.valueCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

fun interface ImpactAction<in T: HitResult> {
    fun tryImpact(level: ServerLevel, projectile: CustomItemProjectile, hit: T): Boolean

    interface Inline<in T: HitResult> : ImpactAction<T> {
        val codec: MapCodec<out Inline<T>>
    }

    data object None : ImpactAction<HitResult> {
        override fun tryImpact(level: ServerLevel, projectile: CustomItemProjectile, hit: HitResult): Boolean = false
    }

    companion object {

        @JvmField
        val BLOCK_INLINE_CODEC: Codec<Inline<BlockHitResult>> = OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION_TYPE
            .byNameCodec()
            .dispatch(Inline<BlockHitResult>::codec) { it }

        @JvmField
        val ENTITY_INLINE_CODEC: Codec<Inline<EntityHitResult>> = OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION_TYPE
            .byNameCodec()
            .dispatch(Inline<EntityHitResult>::codec) { it }

        @JvmField
        val BLOCK_CODEC: Codec<ImpactAction<BlockHitResult>> = RegistryFileCodec.create(
            OmniCrossbowRegistries.BLOCK_IMPACT_ACTION,
            BLOCK_INLINE_CODEC.narrow { "Cannot serialize builtin action" }
        ).valueCodec(OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION)

        @JvmField
        val ENTITY_CODEC: Codec<ImpactAction<EntityHitResult>> = RegistryFileCodec.create(
            OmniCrossbowRegistries.ENTITY_IMPACT_ACTION,
            ENTITY_INLINE_CODEC.narrow { "Cannot serialize builtin action" }
        ).valueCodec(OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION)
    }
}