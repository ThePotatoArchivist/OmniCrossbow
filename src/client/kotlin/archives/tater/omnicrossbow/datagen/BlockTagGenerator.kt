package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowBlocks
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import java.util.concurrent.CompletableFuture

class BlockTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.BlockTagsProvider(output, registryLookupFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
        with (valueLookupBuilder(OmniCrossbowTags.HAS_PREFERRED_TOOL)) {
            +BlockTags.MINEABLE_WITH_PICKAXE
            +BlockTags.MINEABLE_WITH_AXE
            +BlockTags.MINEABLE_WITH_SHOVEL
            +BlockTags.MINEABLE_WITH_HOE
        }
        with (valueLookupBuilder(BlockTags.INSIDE_STEP_SOUND_BLOCKS)) {
            +OmniCrossbowBlocks.HONEY_SLICK
        }
    }
}