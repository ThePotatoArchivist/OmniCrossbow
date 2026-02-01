package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.client.render.AmmoPosition
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Items
import org.joml.Vector3f
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class AmmoPositionGenerator(
    packOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricCodecDataProvider<AmmoPosition.Entries>(packOutput, registriesFuture, PackOutput.Target.RESOURCE_PACK, "", AmmoPosition.ENTRIES_CODEC) {
    override fun configure(
        provider: BiConsumer<Identifier, AmmoPosition.Entries>,
        registryLookup: HolderLookup.Provider
    ) {
        provider.accept(AmmoPosition.PATH, listOf(
            AmmoPosition.Entry(listOf(
                Items.IRON_PICKAXE
            ), ItemTransform(
                Vector3f(0f, 0f, 90f),
                Vector3f(-3 / 16f, 2 / 16f, 1 / 16f),
                Vector3f(1f, 1f, 1f)
            ))
        ))
    }

    override fun getName(): String = "Ammo Positions"

}