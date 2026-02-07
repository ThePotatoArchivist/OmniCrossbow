package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.network.HaircutPayload
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object OmniCrossbowNetworking {
    fun init() {
        with (PayloadTypeRegistry.clientboundPlay()) {
            register(HaircutPayload.TYPE, HaircutPayload.CODEC)
        }
    }
}