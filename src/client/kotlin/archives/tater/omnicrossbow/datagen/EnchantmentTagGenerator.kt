package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.OmniCrossbow
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
        buildTag(MULTICHAMBERED_EXCLUSIVE) {
            +Enchantments.QUICK_CHARGE
            +Enchantments.MULTISHOT
        }
    }

    companion object {
        val MULTICHAMBERED_EXCLUSIVE: TagKey<Enchantment> = TagKey.create(Registries.ENCHANTMENT, OmniCrossbow.id("exclusive_set/multichambered"))
    }
}