package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.world.damagesource.DamageEffects
import net.minecraft.world.damagesource.DamageType

object DamageTypeGenerator : RegistrySetBuilder.RegistryBootstrap<DamageType>  {
    override fun run(registry: BootstrapContext<DamageType>) {
        registry[OmniCrossbowDamageTypes.FIRE_BEAM] = DamageType(
            "omnicrossbow.fire_beam",
            0.1f,
            DamageEffects.BURNING
        )
        registry[OmniCrossbowDamageTypes.FIRE_PROJECTILE] = DamageType(
            "omnicrossbow.fire_projectile",
            0.1f,
            DamageEffects.BURNING
        )
        registry[OmniCrossbowDamageTypes.SONIC_BOOM] = DamageType(
            "omnicrossbow.sonic_boom",
            0.1f
        )
    }
}