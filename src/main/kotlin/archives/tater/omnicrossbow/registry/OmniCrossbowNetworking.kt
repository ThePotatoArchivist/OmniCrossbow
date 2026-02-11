package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.network.AddMovementPayload
import archives.tater.omnicrossbow.network.FireworksPayload
import archives.tater.omnicrossbow.network.HaircutPayload
import archives.tater.omnicrossbow.network.ParticleBeamPayload
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object OmniCrossbowNetworking {
    fun init() {
        with (PayloadTypeRegistry.clientboundPlay()) {
            register(HaircutPayload.TYPE, HaircutPayload.CODEC)
            register(FireworksPayload.TYPE, FireworksPayload.CODEC)
            register(ParticleBeamPayload.TYPE, ParticleBeamPayload.CODEC)
            register(AddMovementPayload.TYPE, AddMovementPayload.CODEC)
        }
    }
}