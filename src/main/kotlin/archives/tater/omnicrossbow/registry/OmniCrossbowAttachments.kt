package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.GrappleFishingHook
import archives.tater.omnicrossbow.projectilebehavior.DelayTracker
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior
import archives.tater.omnicrossbow.util.unverifiedUnitCodec
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.mojang.serialization.Codec

@Suppress("UnstableApiUsage")
object OmniCrossbowAttachments {
    private fun <T: Any> register(path: String, init: AttachmentRegistry.Builder<T>.() -> Unit = {}): AttachmentType<T> =
        AttachmentRegistry.create(OmniCrossbow.id(path), init)

    // temporary field read during projectile initialization in different classes
    @JvmField
    val PROJECTILE_BEHAVIOR = register<ProjectileBehavior>("projectile_behavior")

    @JvmField
    val WIND_CHARGE_EXPLOSION_RADIUS = register<Float>("wind_charge_explosion_radius") {
        persistent(Codec.FLOAT)
    }

    @JvmField
    val DELAYED_SHOTS = register("delayed_shots") {
        syncWith(unverifiedUnitCodec(DelayTracker()), AttachmentSyncPredicate.targetOnly()) // Client only needs to know it's present
    }

    @JvmField
    val CONNECTED_GRAPPLE_HOOKS = register<MutableList<GrappleFishingHook>>("connected_grapple_hooks") {
        initializer(::mutableListOf)
    }

    @JvmField
    val GRAPPLE_NO_HIT_COOLDOWN = register<Int>("grapple_no_hit_cooldown")

    fun init() {

    }
}