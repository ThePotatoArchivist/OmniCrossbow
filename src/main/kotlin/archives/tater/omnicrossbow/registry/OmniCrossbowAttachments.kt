package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent

@Suppress("UnstableApiUsage")
object OmniCrossbowAttachments {
    @JvmField
    val SHOOT_SOUND: AttachmentType<Holder<SoundEvent>> = AttachmentRegistry.create(OmniCrossbow.id("shoot_sound"))

    @JvmField
    val VELOCITY_SCALE: AttachmentType<Float> = AttachmentRegistry.create(OmniCrossbow.id("velocity_scale"))

    fun init() {

    }
}