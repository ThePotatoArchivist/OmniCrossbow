package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.util.valueproviders.ConstantFloat
import net.minecraft.util.valueproviders.FloatProvider
import net.minecraft.util.valueproviders.FloatProviders
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.HitResult

@JvmRecord
data class PlaySound(
    val soundEvent: Holder<SoundEvent>,
    val volume: FloatProvider = ONE,
    val pitch: FloatProvider = ONE,
) : ImpactAction.Inline {

    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        level.playSound(
            null,
            hit.location.x,
            hit.location.y,
            hit.location.z,
            soundEvent,
            projectile.soundSource,
            volume.sample(projectile.random),
            pitch.sample(projectile.random)
        )
        return true
    }

    override val codec: MapCodec<out ImpactAction.Inline> get() = CODEC

    companion object {
        val ONE = ConstantFloat.of(1f)

        val CODEC: MapCodec<PlaySound> = RecordCodecBuilder.mapCodec { it.group(
            SoundEvent.CODEC.fieldOf("sound_event").forGetter(PlaySound::soundEvent),
            FloatProviders.codec(0f, Float.MAX_VALUE).optionalFieldOf("volume", ONE).forGetter(PlaySound::volume),
            FloatProviders.codec(0f, Float.MAX_VALUE).optionalFieldOf("pitch", ONE).forGetter(PlaySound::pitch),
        ).apply(it, ::PlaySound) }
    }
}