package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey

object OmniCrossbowEntityIds {
    private fun create(path: String) = ResourceKey.create(Registries.ENTITY_TYPE, OmniCrossbow.id(path))

    @JvmField val DELEGATE_PROJECTILE = create("delegate_projectile")
    @JvmField val CUSTOM_ITEM_PROJECTILE = create("custom_item_projectile")
    @JvmField val SLIME_BALL = create("slimeball")
    @JvmField val MAGMA_CREAM = create("magma_cream")
    @JvmField val FREEZING_SNOWBALL = create("freezing_snowball")
    @JvmField val CROSSBOW = create("crossbow")
    @JvmField val END_CRYSTAL = create("end_crystal")
    @JvmField val EMER = create("ember")
    @JvmField val BEACON_LASER = create("beacon_laser")
    @JvmField val SPY_ENDER_EYE = create("spy_ender_eye")
    @JvmField val GRAPPLE_FISHING_HOOK = create("grapple_fishing_hook")
}