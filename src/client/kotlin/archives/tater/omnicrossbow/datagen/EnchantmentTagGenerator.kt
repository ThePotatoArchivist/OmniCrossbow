package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import java.util.concurrent.CompletableFuture

class EnchantmentTagGenerator(
    output: FabricPackOutput,
    registryLookupFuture: CompletableFuture<HolderLookup.Provider>
) : FabricTagsProvider<Enchantment>(output, Registries.ENCHANTMENT, registryLookupFuture) {

    private fun buildTag(tag: TagKey<Enchantment>, block: TagAppender<ResourceKey<Enchantment>, Enchantment>.() -> Unit) {
        builder(tag).block()
    }

    override fun addTags(registries: HolderLookup.Provider) {
        buildTag(EnchantmentTags.NON_TREASURE) {
            +OmniCrossbowEnchantments.MULTICHAMBERED
        }
        buildTag(EnchantmentTags.CROSSBOW_EXCLUSIVE) {
            +OmniCrossbowEnchantments.MAGAZINE
            +OmniCrossbowEnchantments.OMNI
            +OmniCrossbowEnchantments.SHARPSHOOTING
        }
        buildTag(OmniCrossbowEnchantments.MAGAZINE_EXCLUSIVE) {
            +EnchantmentTags.CROSSBOW_EXCLUSIVE
            +Enchantments.QUICK_CHARGE
            +OmniCrossbowEnchantments.PUMP_CHARGE
            +OmniCrossbowEnchantments.SHARPSHOOTING
            +OmniCrossbowEnchantments.MULTICHAMBERED
        }
        buildTag(OmniCrossbowEnchantments.MULTICHAMBERED_EXCLUSIVE) {
            +Enchantments.QUICK_CHARGE
            +Enchantments.MULTISHOT
            +OmniCrossbowEnchantments.PUMP_CHARGE
        }
        buildTag(OmniCrossbowEnchantments.PUMP_CHARGE_EXCLUSIVE) {
            +Enchantments.PIERCING
            +Enchantments.MULTISHOT
        }
        buildTag(EnchantmentTags.CURSE) {
            +OmniCrossbowEnchantments.TWIRLING_CURSE
        }
    }

}