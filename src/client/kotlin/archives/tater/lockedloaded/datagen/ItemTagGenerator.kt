package archives.tater.lockedloaded.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class ItemTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.ItemTagsProvider(output, registryLookupFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
    }
}