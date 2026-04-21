package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import archives.tater.omnicrossbow.registry.OmniCrossbowLoot
import archives.tater.omnicrossbow.util.item
import archives.tater.omnicrossbow.util.lootTable
import archives.tater.omnicrossbow.util.pool
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class LootGenerator(
    output: FabricPackOutput,
    private val registryLookupFuture: CompletableFuture<HolderLookup.Provider>,
) : SimpleFabricLootTableSubProvider(output, registryLookupFuture, LootContextParamSets.CHEST) {

    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
        val registries = registryLookupFuture.join()
        output.accept(OmniCrossbowLoot.TRIAL_CHAMBER_INJECT, lootTable {
            pool {
                item(Items.BOOK) {
                    apply(SetEnchantmentsFunction.Builder().withEnchantment(
                        registries.getOrThrow(OmniCrossbowEnchantments.OMNI),
                        exactly(1f)
                    ))
                }
            }
        })
    }
}