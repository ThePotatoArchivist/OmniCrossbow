package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.util.ParticleConfig
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.sendParticles
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.StringRepresentable
import net.minecraft.util.StringRepresentable.EnumCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

@JvmRecord
data class ShowParticle(val particle: ParticleConfig, val anchor: Anchor, val offset: Vec3) : ImpactAction.Inline {
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val pos = when (anchor) {
            Anchor.PROJECTILE -> projectile.position()
            Anchor.HIT -> hit.location
            Anchor.BLOCK -> ((hit as? BlockHitResult)?.blockPos ?: BlockPos.containing(hit.location)).center
        } + offset
        level.sendParticles(particle, pos.x, pos.y, pos.z)
        return true
    }

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    enum class Anchor(private val serializedName: String) : StringRepresentable {
        PROJECTILE("projectile"),
        HIT("hit"),
        BLOCK("block");

        override fun getSerializedName(): String = serializedName

        companion object {
            val CODEC: EnumCodec<Anchor> = StringRepresentable.fromEnum<Anchor> { Anchor.entries.toTypedArray() }
        }
    }

    companion object {
        val CODEC: MapCodec<ShowParticle> = RecordCodecBuilder.mapCodec { it.group(
            ParticleConfig.MAP_CODEC.forGetter(ShowParticle::particle),
            Anchor.CODEC.optionalFieldOf("anchor", Anchor.HIT).forGetter(ShowParticle::anchor),
            Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(ShowParticle::offset),
        ).apply(it, ::ShowParticle) }
    }
}