package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.sounds.SoundEvent

@Suppress("UnstableApiUsage")
object OmniCrossbowAttachments {
    private fun <T: Any> register(path: String, init: AttachmentRegistry.Builder<T>.() -> Unit = {}): AttachmentType<T> =
        AttachmentRegistry.create(OmniCrossbow.id(path), init)

    @JvmField
    val SHOOT_SOUND: AttachmentType<Holder<SoundEvent>> = register("shoot_sound")

    @JvmField
    val VELOCITY_SCALE: AttachmentType<Float> = register("velocity_scale")

    @JvmField
    val RICOCHET_LEVEL: AttachmentType<Byte> = register("ricochet_level") {
        persistent(Codec.BYTE)
        syncWith(ByteBufCodecs.BYTE, AttachmentSyncPredicate.all())
    }

    @JvmField
    val ORIGINAL_PIERCE_COUNT: AttachmentType<Byte> = register("original_pierce_count") {
        persistent(Codec.BYTE)
        syncWith(ByteBufCodecs.BYTE, AttachmentSyncPredicate.all())
    }

    fun init() {

    }
}