package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.util.McUnit
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.mojang.serialization.Codec
import net.minecraft.network.codec.ByteBufCodecs

@Suppress("UnstableApiUsage")
object OmniCrossbowAttachments {
    private fun <T: Any> register(path: String, init: AttachmentRegistry.Builder<T>.() -> Unit = {}): AttachmentType<T> =
        AttachmentRegistry.create(OmniCrossbow.id(path), init)

    // temporary field read during projectile initialization in different classes
    @JvmField
    val PROJECTILE_BEHAVIOR: AttachmentType<ProjectileBehavior> = register("projectile_behavior")

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

    @JvmField
    val IGNORE_OWNER: AttachmentType<McUnit> = register("ignore_owner") {
        persistent(McUnit.CODEC)
        syncWith(McUnit.STREAM_CODEC, AttachmentSyncPredicate.all())
    }

    @JvmField
    val SPINNING_ITEM: AttachmentType<McUnit> = register("spinning_item") {
        syncWith(McUnit.STREAM_CODEC, AttachmentSyncPredicate.all())
    }

    fun init() {

    }
}