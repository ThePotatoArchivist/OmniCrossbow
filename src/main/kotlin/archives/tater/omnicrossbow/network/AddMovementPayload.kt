package archives.tater.omnicrossbow.network

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.plus
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import io.netty.buffer.ByteBuf

@JvmRecord
data class AddMovementPayload(val movement: Vec3) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out AddMovementPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<AddMovementPayload>(OmniCrossbow.id("add_movement"))
        val CODEC: StreamCodec<ByteBuf, AddMovementPayload> = Vec3.STREAM_CODEC.map(::AddMovementPayload, AddMovementPayload::movement)
    }
}

fun Entity.addMovementClient(movement: Vec3) {
    deltaMovement += movement
    if (this is ServerPlayer) ServerPlayNetworking.send(this, AddMovementPayload(movement))
}