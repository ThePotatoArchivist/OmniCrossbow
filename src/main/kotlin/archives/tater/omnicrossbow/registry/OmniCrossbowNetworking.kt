package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.network.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object OmniCrossbowNetworking {
    fun init() {
        with (PayloadTypeRegistry.clientboundPlay()) {
            register(HaircutPayload.TYPE, HaircutPayload.CODEC)
            register(FireworksPayload.TYPE, FireworksPayload.CODEC)
            register(ParticleBeamPayload.TYPE, ParticleBeamPayload.CODEC)
            register(AddMovementPayload.TYPE, AddMovementPayload.CODEC)
            register(ViewSpyEyePayload.TYPE, ViewSpyEyePayload.CODEC)
        }
    }
}