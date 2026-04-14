package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbowClient
import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.core.HolderLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class LangGenerator(packOutput: FabricPackOutput, registryLookup: CompletableFuture<HolderLookup.Provider>) : FabricLanguageProvider(packOutput, registryLookup) {
    override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
        fun add(key: ResourceKey<DamageType>, normal: String? = null, player: String? = null, item: String? = null) {
            val msgId = registryLookup.getOrThrow(key).value().msgId
            normal?.let { translationBuilder.add("death.attack.$msgId", it) }
            player?.let { translationBuilder.add("death.attack.$msgId.player", it) }
            item?.let { translationBuilder.add("death.attack.$msgId.item", it) }
        }

        translationBuilder.addEnchantment(OmniCrossbowEnchantments.OMNI, "Omni")
        add(OmniCrossbowDamageTypes.FIRE_BEAM,
            normal = "%s was caramelized by %s",
            item = "%s was caramelized by %s using %s",
        )
        add(OmniCrossbowDamageTypes.FIRE_PROJECTILE,
            normal = "%s was flambéed by %s",
            item = "%s was flambéed by %s using %s",
        )
        add(OmniCrossbowDamageTypes.SONIC_BOOM,
            normal = "%s was obliterated by a sonically-charged shriek from %s",
            item = "%s was obliterated by a sonically-charged shriek from %s using %s",
        )
        add(OmniCrossbowDamageTypes.BEACON,
            normal = "%s had a bad time at the hands of %s",
            item = "%s had a bad time at the hands of %s using %s",
        )

        translationBuilder.add(OmniCrossbowClient.EYE_HINT, "Press %s to stop observing")
    }


}