package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.datagen.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries

object OmniCrossbowDataGenerator : DataGeneratorEntrypoint {
	override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
		with (registryBuilder) {
			add(Registries.ENCHANTMENT, EnchantmentGenerator)
		}
	}

	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
		with (fabricDataGenerator.createPack()) {
			addProvider(dynamicRegistry("Enchantments", Registries.ENCHANTMENT))
			addProvider(::ItemTagGenerator)
			addProvider(::EnchantmentTagGenerator)
			addProvider(::DamageTagGenerator)
			addProvider(::LangGenerator)
			addProvider(::AmmoPositionGenerator)
			addProvider(::ProjectileBehaviorGenerator)
		}
	}
}