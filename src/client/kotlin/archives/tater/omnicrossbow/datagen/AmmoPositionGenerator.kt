package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.client.render.AmmoPosition
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.Items
import org.joml.Vector3f
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class AmmoPositionGenerator(
    packOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricCodecDataProvider<AmmoPosition.Entries>(packOutput, registriesFuture, PackOutput.Target.RESOURCE_PACK, ".", AmmoPosition.ENTRIES_CODEC) {
    override fun configure(
        provider: BiConsumer<Identifier, AmmoPosition.Entries>,
        registryLookup: HolderLookup.Provider
    ) {
        provider.accept(AmmoPosition.PATH, listOf(
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 90f),
                translation = Vector3f(-3 / 16f, 2 / 16f, 1 / 16f),
            )) {
                +ConventionalItemTags.TOOLS
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 90f),
                translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
            )) {
                +ConventionalItemTags.SHEAR_TOOLS
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 90f),
                translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
            )) {
                +ConventionalItemTags.MELEE_WEAPON_TOOLS
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 90f),
                translation = Vector3f(3 / 16f, -3 / 16f, 1 / 16f),
                scale = Vector3f(2f, 2f, 1f),
            ), ItemDisplayContext.NONE) {
                +ItemTags.SPEARS
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 90f),
                translation = Vector3f(-3 / 16f, 2 / 16f, 1 / 16f),
            )) {
                +ItemTags.AXES
            },

            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 45f),
                translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
            )) {
                +ItemTags.EGGS
                +Items.GLASS_BOTTLE
                +Items.DRAGON_BREATH
                +ConventionalItemTags.BUCKETS
                +ConventionalItemTags.POTIONS
                +ConventionalItemTags.DRINKS
                +ConventionalItemTags.SOUP_FOODS
                +ConventionalItemTags.DUSTS
                +ConventionalItemTags.GUNPOWDERS
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, -90f),
                translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
            )) {
                +ItemTags.FISHES
                +ConventionalItemTags.BONES
                +ConventionalItemTags.RODS
            },
            AmmoPosition.Entry(ItemTransform(
                translation = Vector3f(-1 / 16f, 1 / 16f, 1 / 16f),
            )) {
                +Items.WIND_CHARGE
            },
            AmmoPosition.Entry(AmmoPosition.DEFAULT_TRANSFORM.transform) {
                +Items.PUFFERFISH
            },
            AmmoPosition.Entry(ItemTransform(
                rotation = Vector3f(0f, 0f, 225f),
                translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
            )) {
                +Items.POINTED_DRIPSTONE
            },
        ))
    }

    override fun getName(): String = "Ammo Positions"

}