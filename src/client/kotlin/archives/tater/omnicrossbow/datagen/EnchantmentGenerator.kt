package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.enchantment.LoadMultiple
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantment.*
import net.minecraft.world.item.enchantment.LevelBasedValue
import net.minecraft.world.item.enchantment.effects.SetValue
import java.util.concurrent.CompletableFuture

class EnchantmentGenerator(output: FabricPackOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricDynamicRegistryProvider(output, registriesFuture) {

    override fun configure(registries: HolderLookup.Provider, entries: Entries) {
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT))
    }

    override fun getName(): String = "Enchantments"

    companion object : RegistrySetBuilder.RegistryBootstrap<Enchantment> {
        override fun run(registry: BootstrapContext<Enchantment>) {
            val items = registry.lookup(Registries.ITEM)
            val enchantments = registry.lookup(Registries.ENCHANTMENT)

            fun register(key: ResourceKey<Enchantment>, definition: EnchantmentDefinition, init: Enchantment.Builder.() -> Unit) =
                enchantment(definition).apply(init).build(key.identifier()).also {
                    registry.register(key, it)
                }

            register(OmniCrossbowEnchantments.MULTICHAMBERED, definition(
                items.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE),
                4,
                3,
                dynamicCost(12, 20),
                constantCost(50),
                2,
                EquipmentSlotGroup.MAINHAND
            )) {
                exclusiveWith(enchantments.getOrThrow(EnchantmentTagGenerator.MULTICHAMBERED_EXCLUSIVE))
                withSpecialEffect(OmniCrossbowEnchantmentEffects.LOAD_MULTIPLE, LoadMultiple(LevelBasedValue.perLevel(2f)))
                withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_FIRED_COUNT, SetValue(LevelBasedValue.constant(1f)))
            }
        }

    }
}