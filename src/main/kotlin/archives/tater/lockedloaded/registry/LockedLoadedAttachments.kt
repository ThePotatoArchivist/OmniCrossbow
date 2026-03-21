package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import archives.tater.lockedloaded.util.McUnit
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.mojang.serialization.Codec
import net.minecraft.network.codec.ByteBufCodecs

@Suppress("UnstableApiUsage")
object LockedLoadedAttachments {
    private fun <T: Any> register(path: String, init: AttachmentRegistry.Builder<T>.() -> Unit = {}): AttachmentType<T> =
        AttachmentRegistry.create(LockedLoaded.id(path), init)

    @JvmField
    val RICOCHET_LEVEL = register<Byte>("ricochet_level") {
        persistent(Codec.BYTE)
        syncWith(ByteBufCodecs.BYTE, AttachmentSyncPredicate.all())
    }

    @JvmField
    val ORIGINAL_PIERCE_COUNT = register<Byte>("original_pierce_count") {
        persistent(Codec.BYTE)
        syncWith(ByteBufCodecs.BYTE, AttachmentSyncPredicate.all())
    }

    @JvmField
    val IGNORE_OWNER = register<McUnit>("ignore_owner") {
        persistent(McUnit.CODEC)
        syncWith(McUnit.STREAM_CODEC, AttachmentSyncPredicate.all())
    }

    @JvmField
    val SPINNING_ITEM = register<McUnit>("spinning_item") {
        syncWith(McUnit.STREAM_CODEC, AttachmentSyncPredicate.all())
    }

    fun init() {

    }
}