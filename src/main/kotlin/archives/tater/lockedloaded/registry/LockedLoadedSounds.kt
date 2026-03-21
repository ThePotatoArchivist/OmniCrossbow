package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

object LockedLoadedSounds {
    private fun register(id: Identifier): SoundEvent = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id))

    private fun register(path: String) = register(LockedLoaded.id(path))

    @JvmField val SPIN = register("item.crossbow.spin")
    @JvmField val RICOCHET = register("entity.arrow.ricochet")

    fun init() {}
}