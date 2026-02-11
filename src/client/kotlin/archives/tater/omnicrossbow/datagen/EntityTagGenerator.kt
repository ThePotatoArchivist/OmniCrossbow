package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
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
    }
}