package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.entity.SpyEnderEye
import archives.tater.omnicrossbow.network.*
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object OmniCrossbowNetworking {
    fun init() {
        with (PayloadTypeRegistry.clientboundPlay()) {
            register(HaircutPayload.TYPE, HaircutPayload.CODEC)
            register(FireworksPayload.TYPE, FireworksPayload.CODEC)
            register(ParticleBeamPayload.TYPE, ParticleBeamPayload.CODEC)
            register(AddMovementPayload.TYPE, AddMovementPayload.CODEC)
            register(ViewSpyEyePayload.TYPE, ViewSpyEyePayload.CODEC)
        }
        with (PayloadTypeRegistry.serverboundPlay()) {
            register(SpyEyeInputPayload.TYPE, SpyEyeInputPayload.CODEC)
        }

        ServerPlayNetworking.registerGlobalReceiver(SpyEyeInputPayload.TYPE, SpyEnderEye)
    }
}