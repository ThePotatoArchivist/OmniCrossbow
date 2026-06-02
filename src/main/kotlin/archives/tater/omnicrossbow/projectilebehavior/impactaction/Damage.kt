package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryFixedCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import java.util.*
import kotlin.jvm.optionals.getOrNull


@JvmRecord
data class Damage(val amount: Float, val type: Optional<Holder<DamageType>> = Optional.empty()) : ImpactAction.Inline {

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val entity = (hit as? EntityHitResult)?.entity ?: return false

        entity.hurtServer(level, DamageSource(type.getOrNull() ?: level.registryAccess().getOrThrow(DamageTypes.THROWN), projectile, projectile.owner ?: projectile), amount)

        return true
    }

    override val codec: MapCodec<out Damage> get() = CODEC

    companion object {
        val CODEC: MapCodec<Damage> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("amount").forGetter(Damage::amount),
            RegistryFixedCodec.create(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type").forGetter(Damage::type),
        ).apply(it, ::Damage) }
    }
}