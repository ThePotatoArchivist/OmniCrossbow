package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type
import io.netty.buffer.ByteBuf

@JvmRecord
data class FireworksPayload(val id: Int): CustomPacketPayload {
    override fun type(): Type<out FireworksPayload> = TYPE

    companion object {
        val CODEC: StreamCodec<ByteBuf, FireworksPayload> = StreamCodec.composite(
            ByteBufCodecs.INT, FireworksPayload::id,
            ::FireworksPayload
        )

        val TYPE = Type<FireworksPayload>(OmniCrossbow.id("fireworks"))
    }
}