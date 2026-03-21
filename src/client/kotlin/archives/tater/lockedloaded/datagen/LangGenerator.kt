package archives.tater.lockedloaded.datagen

import archives.tater.lockedloaded.registry.LockedLoadedEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import java.util.concurrent.CompletableFuture

class LangGenerator(packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>) : FabricLanguageProvider(packOutput, registryLookup) {
    override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
        translationBuilder.addEnchantment(LockedLoadedEnchantments.MULTICHAMBERED, "Multichambered")
        translationBuilder.addEnchantment(LockedLoadedEnchantments.PUMP_CHARGE, "Pump Charge")
        translationBuilder.addEnchantment(LockedLoadedEnchantments.MAGAZINE, "Magazine")
        translationBuilder.addEnchantment(LockedLoadedEnchantments.SHARPSHOOTING, "Sharpshooting")
        translationBuilder.addEnchantment(LockedLoadedEnchantments.TWIRLING_CURSE, "Curse of Twirling")
    }


}