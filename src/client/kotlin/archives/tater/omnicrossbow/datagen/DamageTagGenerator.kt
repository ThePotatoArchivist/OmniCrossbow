package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagAppender
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class DamageTagGenerator(
    output: FabricPackOutput,
    registryLookupFuture: CompletableFuture<HolderLookup.Provider>
) : FabricTagsProvider<DamageType>(output, Registries.DAMAGE_TYPE, registryLookupFuture) {

    private fun buildTag(tag: TagKey<DamageType>, block: TagAppender<DamageType>.() -> Unit) {
        builder(tag).block()
    }

    override fun addTags(registries: HolderLookup.Provider) {
        buildTag(DamageTypeTags.BYPASSES_COOLDOWN) {
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.NO_KNOCKBACK) {
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.NO_IMPACT) {
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.IS_FIRE) {
            +OmniCrossbowDamageTypes.FIRE_BEAM
            +OmniCrossbowDamageTypes.FIRE_PROJECTILE
        }
        buildTag(DamageTypeTags.IS_PROJECTILE) {
            +OmniCrossbowDamageTypes.FIRE_PROJECTILE
        }
        buildTag(DamageTypeTags.BYPASSES_SHIELD) {
            +OmniCrossbowDamageTypes.FIRE_BEAM
        }
        buildTag(DamageTypeTags.IGNITES_ARMOR_STANDS) {
            +OmniCrossbowDamageTypes.FIRE_BEAM
            +OmniCrossbowDamageTypes.FIRE_PROJECTILE
        }
        buildTag(DamageTypeTags.PANIC_CAUSES) {
            +OmniCrossbowDamageTypes.FIRE_BEAM
            +OmniCrossbowDamageTypes.FIRE_PROJECTILE
            +OmniCrossbowDamageTypes.SONIC_BOOM
        }
        buildTag(DamageTypeTags.BYPASSES_ARMOR) {
            +OmniCrossbowDamageTypes.SONIC_BOOM
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.BYPASSES_ENCHANTMENTS) {
            +OmniCrossbowDamageTypes.SONIC_BOOM
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.BYPASSES_EFFECTS) {
            +OmniCrossbowDamageTypes.BEACON
        }
        buildTag(DamageTypeTags.BYPASSES_INVULNERABILITY) {
            +OmniCrossbowDamageTypes.BEACON
        }
    }

}