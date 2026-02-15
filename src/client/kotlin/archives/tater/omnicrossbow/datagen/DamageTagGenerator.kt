package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class DamageTagGenerator(
    output: FabricPackOutput,
    registryLookupFuture: CompletableFuture<HolderLookup.Provider>
) : FabricTagsProvider<DamageType>(output, Registries.DAMAGE_TYPE, registryLookupFuture) {

    private fun buildTag(tag: TagKey<DamageType>, block: TagAppender<ResourceKey<DamageType>, DamageType>.() -> Unit) {
        builder(tag).block()
    }

    override fun addTags(registries: HolderLookup.Provider) {
        buildTag(DamageTypeTags.BYPASSES_COOLDOWN) {
            forceAddTag(DamageTypeTags.IS_PROJECTILE)
        }
        buildTag(DamageTypeTags.IS_FIRE) {
            add(OmniCrossbowDamageTypes.FIRE_BEAM)
            add(OmniCrossbowDamageTypes.FIRE_PROJECTILE)
        }
        buildTag(DamageTypeTags.IS_PROJECTILE) {
            add(OmniCrossbowDamageTypes.FIRE_PROJECTILE)
        }
        buildTag(DamageTypeTags.BYPASSES_SHIELD) {
            add(OmniCrossbowDamageTypes.FIRE_BEAM)
        }
        buildTag(DamageTypeTags.IGNITES_ARMOR_STANDS) {
            add(OmniCrossbowDamageTypes.FIRE_BEAM)
            add(OmniCrossbowDamageTypes.FIRE_PROJECTILE)
        }
        buildTag(DamageTypeTags.PANIC_CAUSES) {
            add(OmniCrossbowDamageTypes.FIRE_BEAM)
            add(OmniCrossbowDamageTypes.FIRE_PROJECTILE)
            add(OmniCrossbowDamageTypes.SONIC_BOOM)
        }
        buildTag(DamageTypeTags.BYPASSES_ARMOR) {
            add(OmniCrossbowDamageTypes.SONIC_BOOM)
        }
        buildTag(DamageTypeTags.BYPASSES_ENCHANTMENTS) {
            add(OmniCrossbowDamageTypes.SONIC_BOOM)
        }
    }

}