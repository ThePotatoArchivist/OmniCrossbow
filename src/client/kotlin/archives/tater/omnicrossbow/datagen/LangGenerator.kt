package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class LangGenerator(packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>) : FabricLanguageProvider(packOutput, registryLookup) {
    override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
        translationBuilder.addEnchantment(OmniCrossbowEnchantments.MULTICHAMBERED, "Multichambered")
        translationBuilder.addEnchantment(OmniCrossbowEnchantments.PUMP_CHARGE, "Pump Charge")
        translationBuilder.addEnchantment(OmniCrossbowEnchantments.MAGAZINE, "Magazine")
        translationBuilder.addEnchantment(OmniCrossbowEnchantments.OMNI, "Omni")
        translationBuilder.addEnchantment(OmniCrossbowEnchantments.SHARPSHOOTING, "Sharpshooting")
    }
}