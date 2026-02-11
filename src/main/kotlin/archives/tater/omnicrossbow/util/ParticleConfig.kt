package archives.tater.omnicrossbow.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs

@JvmRecord
data class ParticleConfig(
    val particle: ParticleOptions,
    val count: Int,
    val dx: Double = 0.0,
    val dy: Double = 0.0,
    val dz: Double = 0.0,
    val speed: Double = 0.0,
) {
    companion object {
        val MAP_CODEC: MapCodec<ParticleConfig> = RecordCodecBuilder.mapCodec { it.group(
            PARTICLE_OPTIONS_SHORT_CODEC.fieldOf(ParticleConfig::particle),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf(ParticleConfig::count),
            NON_NEGATIVE_DOUBLE.fieldOf(ParticleConfig::dx),
            NON_NEGATIVE_DOUBLE.fieldOf(ParticleConfig::dy),
            NON_NEGATIVE_DOUBLE.fieldOf(ParticleConfig::dz),
            NON_NEGATIVE_DOUBLE.fieldOf(ParticleConfig::speed),
        ).apply(it, ::ParticleConfig) }

        val CODEC: Codec<ParticleConfig> = MAP_CODEC.codec()
    }
}

fun ServerLevel.sendParticles(particle: ParticleConfig, x: Double, y: Double, z: Double) {
    sendParticles(particle.particle, x, y, z, particle.count, particle.dx, particle.dy, particle.dz, particle.speed)
}