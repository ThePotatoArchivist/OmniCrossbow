package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

object OmniCrossbowSounds {
    private fun register(id: Identifier): SoundEvent = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id))

    private fun register(path: String) = register(OmniCrossbow.id(path))

    @JvmField val SPIN = register("item.crossbow.spin")

    fun init() {}
}