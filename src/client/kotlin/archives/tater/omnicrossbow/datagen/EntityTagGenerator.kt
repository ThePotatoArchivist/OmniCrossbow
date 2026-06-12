package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEntityIds
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags
import net.minecraft.core.HolderLookup
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.entity.EntityTypeIds
import java.util.concurrent.CompletableFuture

class EntityTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.EntityTypeTagsProvider(output, registryLookupFuture) {
    override fun addTags(registries: HolderLookup.Provider) {
        with (builder(OmniCrossbowTags.CAN_ALWAYS_EQUIP)) {
            +EntityTypeIds.PLAYER
            +EntityTypeIds.ZOMBIE
            +EntityTypeIds.HUSK
            +EntityTypeIds.DROWNED
            +EntityTypeIds.ZOMBIE_VILLAGER
            +EntityTypeIds.PIGLIN
            +EntityTypeIds.PIGLIN_BRUTE
            +EntityTypeIds.ZOMBIFIED_PIGLIN
            +EntityTypeIds.SKELETON
            +EntityTypeIds.STRAY
            +EntityTypeIds.BOGGED
        }
        with (builder(OmniCrossbowTags.EXTRA_BEACON_DAMAGE)) {
            +EntityTypeIds.PLAYER
            +EntityTypeIds.WITHER
        }
        with (builder(OmniCrossbowTags.UNCAPPED_BEACON_DAMAGE)) {
            +EntityTypeIds.WITHER
        }
        with (builder(OmniCrossbowTags.GRAPPLE_UNMOVEABLE)) {
            +EntityTypeIds.SHULKER
            +EntityTypeIds.END_CRYSTAL
            +EntityTypeIds.PHANTOM
            +EntityTypeIds.GHAST
            +EntityTypeIds.CREAKING
            +ConventionalEntityTypeTags.BOSSES
        }
        with (builder(OmniCrossbowTags.NON_FEEDABLE)) {
            +EntityTypeIds.ITEM_FRAME
            +EntityTypeIds.GLOW_ITEM_FRAME
        }
        with (builder(EntityTypeTags.IMPACT_PROJECTILES)) {
            +OmniCrossbowEntityIds.END_CRYSTAL
            +OmniCrossbowEntityIds.SLIME_BALL
            +OmniCrossbowEntityIds.MAGMA_CREAM
            +OmniCrossbowEntityIds.FREEZING_SNOWBALL
            +OmniCrossbowEntityIds.CUSTOM_ITEM_PROJECTILE
        }
        with (builder(EntityTypeTags.REDIRECTABLE_PROJECTILE)) {
            +OmniCrossbowEntityIds.END_CRYSTAL
        }
    }
}
