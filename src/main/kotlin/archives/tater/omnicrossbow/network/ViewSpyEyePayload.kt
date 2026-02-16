package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import io.netty.buffer.ByteBuf

@JvmRecord
data class ViewSpyEyePayload(val entityId: Int) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out ViewSpyEyePayload>  = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ViewSpyEyePayload>(OmniCrossbow.id("view_spy_eye"))

        val CODEC: StreamCodec<ByteBuf, ViewSpyEyePayload> = ByteBufCodecs.INT.map(::ViewSpyEyePayload, ViewSpyEyePayload::entityId)
    }
}