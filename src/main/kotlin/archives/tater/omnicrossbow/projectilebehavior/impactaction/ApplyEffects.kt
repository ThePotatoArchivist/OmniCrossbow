package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

@JvmRecord
data class ApplyEffects(val effects: List<MobEffectInstance>) : ImpactAction.Inline {

    constructor(vararg effects: MobEffectInstance) : this(effects.toList())

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val entity = (hit as? EntityHitResult)?.entity as? LivingEntity ?: return false
        for (effect in effects)
            entity.addEffect(MobEffectInstance(effect))
        return true
    }

    override val codec: MapCodec<out ApplyEffects> get() = CODEC

    companion object {
        val CODEC: MapCodec<ApplyEffects> = RecordCodecBuilder.mapCodec { it.group(
            MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(ApplyEffects::effects)
        ).apply(it, ::ApplyEffects) }
    }
}