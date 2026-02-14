package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.DelayedShot
import archives.tater.omnicrossbow.util.McUnit
import archives.tater.omnicrossbow.util.unverifiedUnitCodec
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
    val PROJECTILE_BEHAVIOR = register<ProjectileBehavior>("projectile_behavior")

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

    @JvmField
    val WIND_CHARGE_EXPLOSION_RADIUS = register<Float>("wind_charge_explosion_radius") {
        persistent(Codec.FLOAT)
    }

    @JvmField
    val DELAYED_SHOTS = register<DelayedShot.Tracker>("delayed_shots") {
        syncWith(unverifiedUnitCodec(DelayedShot.Tracker()), AttachmentSyncPredicate.targetOnly()) // Client only needs to know it's present
    }

    fun init() {

    }
}