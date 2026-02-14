package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowBlocks
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators

class ModelGenerator(output: FabricPackOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(blockModelGenerators: BlockModelGenerators) {
        blockModelGenerators.createRotatableColumn(OmniCrossbowBlocks.HONEY_SLICK)
    }

    override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {
    }
}