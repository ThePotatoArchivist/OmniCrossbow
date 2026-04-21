package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.item.enchantment.providers.SingleEnchantment
import java.util.concurrent.CompletableFuture

class EnchantmentProviderGenerator(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) : FabricDynamicRegistryProvider(output, registriesFuture) {
    override fun configure(
        registries: HolderLookup.Provider,
        entries: Entries
    ) {
        entries.add(
            OmniCrossbowEnchantments.RAID_PILLAGER_FINAL_WAVE_UNIQUE,
            SingleEnchantment(registries.getOrThrow(OmniCrossbowEnchantments.OMNI), ConstantInt(1))
        )
    }

    override fun getName(): String = "Enchantment Provider"
}