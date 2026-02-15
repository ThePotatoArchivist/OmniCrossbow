package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageType

object OmniCrossbowDamageTypes {
    private fun of(path: String): ResourceKey<DamageType> = ResourceKey.create(Registries.DAMAGE_TYPE, OmniCrossbow.id(path))

    @JvmField
    val FIRE_PROJECTILE = of("fire_projectile")
    @JvmField
    val FIRE_BEAM = of("fire_beam")
    @JvmField
    val SONIC_BOOM = of("sonic_boom")
}