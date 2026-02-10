package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.ParticleConfig
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3

@JvmRecord
data class ParticleBeamPayload(
    val particle: ParticleOptions,
    val start: Vec3,
    val end: Vec3,
    val step: Double = 1.0,
    val randomness: Double = 0.0,
    val countPerPos: Int = 1,
    val dx: Float = 0f,
    val dy: Float = 0f,
    val dz: Float = 0f,
    val speed: Float = 0f,
) : CustomPacketPayload {

    constructor(particle: ParticleConfig, start: Vec3, end: Vec3, step: Double, randomness: Double) : this(
        particle.particle,
        start,
        end,
        step,
        randomness,
        particle.count,
        particle.dx.toFloat(),
        particle.dy.toFloat(),
        particle.dz.toFloat(),
        particle.speed.toFloat()
    )

    override fun type(): CustomPacketPayload.Type<out ParticleBeamPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ParticleBeamPayload>(OmniCrossbow.id("particle_beam"))

        val CODEC: StreamCodec<RegistryFriendlyByteBuf, ParticleBeamPayload> = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, ParticleBeamPayload::particle,
            Vec3.STREAM_CODEC, ParticleBeamPayload::start,
            Vec3.STREAM_CODEC, ParticleBeamPayload::end,
            ByteBufCodecs.DOUBLE, ParticleBeamPayload::step,
            ByteBufCodecs.DOUBLE, ParticleBeamPayload::randomness,
            ByteBufCodecs.INT, ParticleBeamPayload::countPerPos,
            ByteBufCodecs.FLOAT, ParticleBeamPayload::dx,
            ByteBufCodecs.FLOAT, ParticleBeamPayload::dy,
            ByteBufCodecs.FLOAT, ParticleBeamPayload::dz,
            ByteBufCodecs.FLOAT, ParticleBeamPayload::speed,
            ::ParticleBeamPayload
        )
    }
}