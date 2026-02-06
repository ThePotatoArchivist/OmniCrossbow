package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class ItemTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.ItemTagsProvider(output, registryLookupFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
        with (valueLookupBuilder(OmniCrossbowTags.BUILTIN_PROJECTILES)) {
            +ItemTags.ARROWS
            +Items.FIREWORK_ROCKET
            // Modded ammo will go here
        }
    }
}