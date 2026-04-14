package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.FloatProvider
import net.minecraft.util.valueproviders.FloatProviders
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level.ExplosionInteraction
import net.minecraft.world.phys.HitResult

@JvmRecord
data class Explode(
    val radius: FloatProvider,
    val fire: Boolean = false,
    val interaction: ExplosionInteraction = ExplosionInteraction.TNT
) : ImpactAction.Inline {
    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack,
    ): Boolean {
        level.explode(projectile, hit.location.x, hit.location.y, hit.location.z, radius.sample(projectile.random), fire, interaction)
        return true
    }

    companion object {
        val CODEC: MapCodec<Explode> = RecordCodecBuilder.mapCodec { it.group(
            FloatProviders.codec(0f, Float.MAX_VALUE).fieldOf("radius").forGetter(Explode::radius),
            Codec.BOOL.optionalFieldOf("fire", false).forGetter(Explode::fire),
            ExplosionInteraction.CODEC.optionalFieldOf("interaction", ExplosionInteraction.TNT).forGetter(Explode::interaction)
        ).apply(it, ::Explode) }
    }
}