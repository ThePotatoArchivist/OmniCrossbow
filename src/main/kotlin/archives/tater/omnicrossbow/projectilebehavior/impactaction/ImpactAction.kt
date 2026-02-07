package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.projectilebehavior.ItemFiltered
import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.narrow
import archives.tater.omnicrossbow.util.valueCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

fun interface ImpactAction {
    fun tryImpact(level: ServerLevel, projectile: CustomItemProjectile, hit: HitResult, originalItem: ItemStack): Boolean

    interface Inline : ImpactAction {
        val codec: MapCodec<out Inline>
    }

    data object None : ImpactAction {
        override fun tryImpact(
            level: ServerLevel,
            projectile: CustomItemProjectile,
            hit: HitResult,
            originalItem: ItemStack
        ): Boolean = false
    }

    companion object {

        @JvmField
        val INLINE_CODEC: Codec<Inline> = OmniCrossbowBuiltinRegistries.IMPACT_ACTION_TYPE
            .byNameCodec()
            .dispatch(Inline::codec) { it }

        @JvmField
        val CODEC: Codec<ImpactAction> = RegistryFileCodec.create(
            OmniCrossbowRegistries.IMPACT_ACTION,
            INLINE_CODEC.narrow { "Cannot serialize builtin action" }
        ).valueCodec(OmniCrossbowBuiltinRegistries.IMPACT_ACTION)

        @JvmField
        val ITEM_FILTERED_CODEC = ItemFiltered.createCodec(CODEC)

        fun getAction(level: Level, stack: ItemStack): ImpactAction =
            ItemFiltered.getFirst(level.registryAccess(), OmniCrossbowRegistries.IMPACT_BEHAVIOR, stack) ?: None
    }
}