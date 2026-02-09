package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.entity.createFakePlayer
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class Damage(val baseAttackDamage: Float = 1f) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val entity = (hit as? EntityHitResult)?.entity ?: return false

        val player = createFakePlayer(level, projectile).attack(entity)
        return true
    }

    override val codec: MapCodec<out Damage> get() = CODEC

    companion object {
        val CODEC: MapCodec<Damage> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("base_attack_damage", 1f).forGetter(Damage::baseAttackDamage)
        ).apply(it, ::Damage) }
    }
}