package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3
import io.netty.buffer.ByteBuf

@JvmRecord
data class SpyEyeInputPayload(val input: Vec3) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out SpyEyeInputPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<SpyEyeInputPayload>(OmniCrossbow.id("spy_eye_input"))
        val CODEC: StreamCodec<ByteBuf, SpyEyeInputPayload> = Vec3.STREAM_CODEC.map(::SpyEyeInputPayload, SpyEyeInputPayload::input)
    }
}