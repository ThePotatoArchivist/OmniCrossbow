package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.entity.EntityType
import java.util.concurrent.CompletableFuture

class EntityTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.EntityTypeTagsProvider(output, registryLookupFuture) {
    override fun addTags(registries: HolderLookup.Provider) {
        with (valueLookupBuilder(OmniCrossbowTags.CAN_ALWAYS_EQUIP)) {
            +EntityType.PLAYER
            +EntityType.ZOMBIE
            +EntityType.HUSK
            +EntityType.DROWNED
            +EntityType.ZOMBIE_VILLAGER
            +EntityType.PIGLIN
            +EntityType.PIGLIN_BRUTE
            +EntityType.ZOMBIFIED_PIGLIN
            +EntityType.SKELETON
            +EntityType.STRAY
            +EntityType.BOGGED
        }
        with (valueLookupBuilder(EntityTypeTags.IMPACT_PROJECTILES)) {
            +OmniCrossbowEntities.END_CRYSTAL
            +OmniCrossbowEntities.SLIME_BALL
            +OmniCrossbowEntities.MAGMA_CREAM
            +OmniCrossbowEntities.FREEZING_SNOWBALL
            +OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE
        }
        with (valueLookupBuilder(EntityTypeTags.REDIRECTABLE_PROJECTILE)) {
            +OmniCrossbowEntities.END_CRYSTAL
        }
    }
}