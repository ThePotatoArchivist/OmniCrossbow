package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.plus
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import io.netty.buffer.ByteBuf

@JvmRecord
data class AddMovementPayload(val movement: Vec3, val resetFalling: Boolean = false) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out AddMovementPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<AddMovementPayload>(OmniCrossbow.id("add_movement"))
        val CODEC: StreamCodec<ByteBuf, AddMovementPayload> = StreamCodec.composite(
            Vec3.STREAM_CODEC, AddMovementPayload::movement,
            ByteBufCodecs.BOOL, AddMovementPayload::resetFalling,
            ::AddMovementPayload
        )
    }
}

fun Entity.addMovementClient(movement: Vec3, resetFalling: Boolean = false) {
    if (resetFalling && deltaMovement.y < 0)
        deltaMovement = deltaMovement.multiply(1.0, 0.0, 1.0)
    deltaMovement += movement
    if (this is ServerPlayer) ServerPlayNetworking.send(this, AddMovementPayload(movement))
}