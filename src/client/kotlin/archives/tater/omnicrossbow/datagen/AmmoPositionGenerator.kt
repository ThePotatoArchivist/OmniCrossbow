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
        provider.accept(AmmoPosition.PATH, buildList {
            generateTransforms(this)
        })
    }

    private fun generateTransforms(builder: MutableList<AmmoPosition.Entry>) {
        fun add(
            rotation: Vector3f = Vector3f(),
            translation: Vector3f = Vector3f(),
            scale: Vector3f = Vector3f(1f),
            displayContext: ItemDisplayContext = ItemDisplayContext.FIXED,
            init: AmmoPosition.Entry.ItemListBuilder.() -> Unit
        ) = builder.add(AmmoPosition.Entry(ItemTransform(rotation, translation, scale), displayContext, init))

        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(-3 / 16f, 2 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.TOOLS
        }
        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.SHEAR_TOOLS
        }
        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.MELEE_WEAPON_TOOLS
        }
        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(3 / 16f, -3 / 16f, 1 / 16f),
            scale = Vector3f(2f, 2f, 1f),
            displayContext = ItemDisplayContext.NONE
        ) {
            +ItemTags.SPEARS
        }
        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(-3 / 16f, 2 / 16f, 1 / 16f),
        ) {
            +ItemTags.AXES
            +Items.AMETHYST_SHARD
        }
        add(
            translation = Vector3f(-4 / 16f, 4 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.CROSSBOW_TOOLS
        }

        add(
            rotation = Vector3f(0f, 0f, -90f),
            translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
        ) {
            +ItemTags.FISHES
            +ConventionalItemTags.BONES
            +ConventionalItemTags.RODS
        }
        add(
            translation = Vector3f(-1 / 16f, 1 / 16f, 1 / 16f),
        ) {
            +Items.WIND_CHARGE
            +Items.FIREWORK_STAR
        }
        add(
            translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f)
        ) {
            +Items.SNOWBALL
            +Items.SLIME_BALL
            +Items.MAGMA_CREAM
            +Items.FIRE_CHARGE
            +Items.ENDER_PEARL
            +Items.ENDER_EYE
        }
        add(
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
        ) {
            +Items.PUFFERFISH
            +Items.ECHO_SHARD
            +Items.PRISMARINE_SHARD
            +Items.COBWEB
            +Items.END_CRYSTAL
            +Items.BONE_MEAL
            +Items.CHORUS_FRUIT
        }
        add(
            rotation = Vector3f(0f, 0f, -90f),
            translation = Vector3f(-1 / 16f, 1 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.CARROT_CROPS
            +ConventionalItemTags.POTATO_CROPS
            +ConventionalItemTags.BEETROOT_CROPS
            +Items.INK_SAC
            +Items.GLOW_INK_SAC
        }
        add(
            rotation = Vector3f(0f, 0f, 90f),
            translation = Vector3f(-3 / 16f, 1 / 16f, 1 / 16f),
        ) {
            +Items.FEATHER
        }
        add(
            rotation = Vector3f(0f, 0f, 225f),
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
        ) {
            +Items.POINTED_DRIPSTONE
        }
        add(
            rotation = Vector3f(0f, 0f, 45f),
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
            scale = Vector3f(2f),
        ) {
            +Items.END_ROD
        }
        add(
            rotation = Vector3f(0f, 0f, 225f),
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
            scale = Vector3f(2f),
        ) {
            +ItemTags.LIGHTNING_RODS
        }
        add(
            translation = Vector3f(-4 / 16f, 3 / 16f, 1 / 16f),
            scale = Vector3f(-1f, 1f, -1f),
        ) {
            +ItemTags.BOATS
        }
        add(
            translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
            scale = Vector3f(-1f, 1f, -1f),
        ) {
            +Items.MINECART
            +Items.CHEST_MINECART
            +Items.COMMAND_BLOCK_MINECART
            +Items.FURNACE_MINECART
            +Items.HOPPER_MINECART
            +Items.TNT_MINECART
        }
        add(
            rotation = Vector3f(0f, 0f, 45f),
            translation = Vector3f(-2 / 16f, 2 / 16f, 1 / 16f),
        ) {
            +ConventionalItemTags.BUCKETS
            +ConventionalItemTags.DRINK_CONTAINING_BOTTLE
            +ConventionalItemTags.BOTTLE_POTIONS
            +Items.DRAGON_BREATH
            +Items.EXPERIENCE_BOTTLE
            +Items.OMINOUS_BOTTLE
            +Items.GLASS_BOTTLE
        }
    }

    override fun getName(): String = "Ammo Positions"

}