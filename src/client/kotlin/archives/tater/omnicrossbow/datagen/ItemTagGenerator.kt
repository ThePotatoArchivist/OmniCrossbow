package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.references.BlockItemIds
import net.minecraft.references.ItemIds
import net.minecraft.tags.ItemTags
import java.util.concurrent.CompletableFuture

class ItemTagGenerator(output: FabricPackOutput, registryLookupFuture: CompletableFuture<HolderLookup.Provider>) :
    FabricTagsProvider.ItemTagsProvider(output, registryLookupFuture) {

    override fun addTags(registries: HolderLookup.Provider) {
        with (builder(OmniCrossbowTags.BUILTIN_PROJECTILES)) {
            +ItemTags.ARROWS
            +ItemIds.FIREWORK_ROCKET
            // Modded ammo will go here
        }
        with (builder(OmniCrossbowTags.CREATIVE_INTANGIBLE_PROJECTILES)) {
            +OmniCrossbowTags.BUILTIN_PROJECTILES
        }
        with(builder(OmniCrossbowTags.MOB_RANDOM_AMMO)) {
            +ItemIds.FIRE_CHARGE
            +BlockItemIds.WITHER_SKELETON_SKULL
            +ItemIds.BLAZE_ROD
            +ItemIds.BLAZE_POWDER
            +ItemIds.SLIME_BALL
            +ItemIds.MAGMA_CREAM
            +ItemIds.ENDER_PEARL
            +BlockItemIds.COBWEB
            +ItemIds.SNOWBALL
            +ItemIds.EGG
            +ItemIds.HONEY_BOTTLE
            +ItemIds.BREEZE_ROD
            +ItemIds.WIND_CHARGE
            +ItemIds.AMETHYST_SHARD
            +ItemIds.GUNPOWDER
            +ItemIds.SALMON_BUCKET
            +BlockItemIds.CARVED_PUMPKIN
            +BlockItemIds.DIRT
            +ItemIds.WOODEN_SWORD
            +ItemIds.BRICK
            +ItemIds.IRON_SHOVEL
            +ItemIds.STONE_SPEAR
            +BlockItemIds.DAMAGED_ANVIL
            +ItemIds.CHORUS_FRUIT
            +ItemIds.EXPERIENCE_BOTTLE
            +ItemIds.TRIDENT
            +ItemIds.INK_SAC
            +BlockItemIds.GLOW_BERRY_CROP
        }
        with (builder(OmniCrossbowTags.MOB_NON_INTANGIBLE_AMMO)) {
            +BlockItemIds.DIRT
            +BlockItemIds.COBWEB
            +BlockItemIds.DAMAGED_ANVIL
            +BlockItemIds.CARVED_PUMPKIN
            +ItemIds.BRICK
        }
    }
}