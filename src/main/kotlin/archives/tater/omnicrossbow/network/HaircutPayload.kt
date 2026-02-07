package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import io.netty.buffer.ByteBuf

data object HaircutPayload : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    val TYPE = CustomPacketPayload.Type<HaircutPayload>(OmniCrossbow.id("haircut"))
    val CODEC: StreamCodec<ByteBuf, HaircutPayload> = StreamCodec.unit(this)
}