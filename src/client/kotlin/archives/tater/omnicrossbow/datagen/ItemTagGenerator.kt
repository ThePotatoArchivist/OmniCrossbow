package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class ItemTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.ItemTagsProvider(output, registryLookupFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
        with (valueLookupBuilder(OmniCrossbowTags.BUILTIN_PROJECTILES)) {
            +ItemTags.ARROWS
            +Items.FIREWORK_ROCKET
            // Modded ammo will go here
        }
        with(valueLookupBuilder(OmniCrossbowTags.MOB_RANDOM_AMMO)) {
            +Items.FIRE_CHARGE
            +Items.WITHER_SKELETON_SKULL
            +Items.BLAZE_POWDER
            +Items.SLIME_BALL
            +Items.MAGMA_CREAM
            +Items.ENDER_PEARL
            +Items.COBWEB
            +Items.SNOWBALL
            +Items.EGG
            +Items.HONEY_BOTTLE
            +Items.BREEZE_ROD
            +Items.WIND_CHARGE
            +Items.AMETHYST_SHARD
            +Items.GUNPOWDER
            +Items.SALMON_BUCKET
            +Items.CARVED_PUMPKIN
            +Items.DIRT
            +Items.WOODEN_SWORD
            +Items.BRICK
            +Items.IRON_SHOVEL
            +Items.STONE_SPEAR
            +Items.DAMAGED_ANVIL
            +Items.CHORUS_FRUIT
            +Items.EXPERIENCE_BOTTLE
            +Items.TRIDENT
        }
    }
}