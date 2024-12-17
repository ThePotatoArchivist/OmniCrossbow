package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.util.OmniUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class OmniCrossbowRenderer {

    public static final ModelIdentifier DYNAMIC_CROSSBOW = new ModelIdentifier(OmniCrossbow.MOD_ID, "dynamic_crossbow", "inventory");
    public static final ModelIdentifier PULLED_CROSSBOW = new ModelIdentifier(OmniCrossbow.MOD_ID, "pulled_crossbow", "inventory");

    public static void renderCrossbow(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        var world = MinecraftClient.getInstance().world;
        var projectile = OmniUtil.getMainProjectile(stack);
        var loadedModel = itemRenderer.getModels().getModelManager().getModel(PULLED_CROSSBOW);
        var leftHanded = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5); // go to center
        loadedModel.getTransformation().getTransformation(mode).apply(leftHanded, matrices);
        itemRenderer.renderItem(stack, ModelTransformationMode.NONE, false, matrices, vertexConsumers, light, overlay, loadedModel);
        matrices.translate(0, 0, 0.0625); // up one pixel
        itemSpecificTransform(projectile.getItem(), matrices);
        matrices.scale(-1, 1, -1); // rotate 180 on y
        itemRenderer.renderItem(projectile, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    public static void register() {
        BuiltinItemRendererRegistry.INSTANCE.register(Items.CROSSBOW, OmniCrossbowRenderer::renderCrossbow);

        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(DYNAMIC_CROSSBOW, PULLED_CROSSBOW));
    }

    public static boolean useDynamic(ItemStack maybeCrossbow) {
        if (!maybeCrossbow.isOf(Items.CROSSBOW)) return false;
        var projectile = OmniUtil.getMainProjectile(maybeCrossbow);
        return !projectile.isEmpty() && !(projectile.getItem() instanceof ArrowItem) && !(projectile.getItem() instanceof FireworkRocketItem);
    }

    // TODO make configurable
    // TODO dripstone
    private static final Set<Item> OFFSET_NO_ROTATE = Set.of(
            Items.ECHO_SHARD,
            Items.CROSSBOW,
            Items.PRISMARINE_SHARD,
            Items.GLISTERING_MELON_SLICE,
            Items.BOW,
            Items.COBWEB
    );
    private static final Set<Item> OFFSET_ROTATE_COUNTERCLOCKWISE = Stream.concat(
            Registries.ITEM.stream().filter(item -> item instanceof SwordItem),
            Stream.of(
                    Items.STICK,
                    Items.BONE,
                    Items.BLAZE_ROD,
                    Items.BAMBOO,
                    Items.SHEARS,
                    Items.TRIDENT,
                    Items.MELON_SLICE,
                    Items.BREAD,
                    Items.ROTTEN_FLESH,
                    Items.BRUSH,
                    Items.FISHING_ROD,
                    Items.CARROT_ON_A_STICK,
                    Items.WARPED_FUNGUS_ON_A_STICK,
                    Items.WHEAT,
                    Items.SUGAR_CANE,
                    Items.FEATHER,
                    Items.KELP,
                    Items.SPYGLASS,
                    Items.DRIED_KELP,
                    Items.CHICKEN,
                    Items.COOKED_CHICKEN
            )
    ).collect(Collectors.toUnmodifiableSet());
    private static final Set<Item> SHIFT_ROTATE_COUNTERCLOCKWISE = Stream.concat(
            Registries.ITEM.stream().filter(item -> item instanceof ToolItem && !(item instanceof SwordItem)),
            Stream.of(
                    Items.AMETHYST_SHARD
            )
    ).collect(Collectors.toSet());
    private static final Set<Item> OFFSET_ROTATE_CLOCKWISE = Stream.concat(
            Registries.ITEM.stream().filter(item -> item instanceof BannerPatternItem || (item instanceof SmithingTemplateItem && item != Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),
            Stream.of(
                    Items.COD,
                    Items.SALMON,
                    Items.COOKED_COD,
                    Items.COOKED_SALMON,
                    Items.TROPICAL_FISH,
                    Items.BEEF,
                    Items.COOKED_BEEF,
                    Items.PORKCHOP,
                    Items.COOKED_PORKCHOP,
                    Items.MUTTON,
                    Items.COOKED_MUTTON,
                    Items.CARROT,
                    Items.GOLDEN_CARROT,
                    Items.BEETROOT,
                    Items.PAPER,
                    Items.RABBIT_FOOT,
                    Items.NAME_TAG
            )
    ).collect(Collectors.toUnmodifiableSet());
    private static final Set<Item> NO_ROTATE = Set.of(
            Items.ENDER_PEARL,
            Items.ENDER_EYE,
            Items.SNOWBALL,
            Items.SLIME_BALL,
            Items.MAGMA_CREAM,
            Items.CLOCK,
            Items.HEART_OF_THE_SEA,
            Items.NAUTILUS_SHELL,
            Items.BONE_MEAL,
            Items.POPPED_CHORUS_FRUIT,
            Items.FIREWORK_STAR,
            Items.PUFFERFISH,
            Items.FIRE_CHARGE,
            Items.CHORUS_FRUIT
    );
    private static final Set<Item> FLIPPED_OFFSET = Stream.concat(
            Registries.ITEM.stream().filter(item -> item instanceof BoatItem || item instanceof MinecartItem),
            Stream.of(
                    Items.IRON_INGOT,
                    Items.GOLD_INGOT,
                    Items.COPPER_INGOT,
                    Items.NETHERITE_INGOT,
                    Items.BRICK,
                    Items.NETHER_BRICK,
                    Items.BOOK,
                    Items.ENCHANTED_BOOK,
                    Items.WRITABLE_BOOK,
                    Items.WRITTEN_BOOK,
                    Items.KNOWLEDGE_BOOK
            )
    ).collect(Collectors.toSet());

    public static void itemSpecificTransform(Item projectile, MatrixStack matrices) {
        var isOffset = OFFSET_NO_ROTATE.contains(projectile) || OFFSET_ROTATE_CLOCKWISE.contains(projectile) || OFFSET_ROTATE_COUNTERCLOCKWISE.contains(projectile) || FLIPPED_OFFSET.contains(projectile);

        if (SHIFT_ROTATE_COUNTERCLOCKWISE.contains(projectile) || FLIPPED_OFFSET.contains(projectile))
            matrices.translate(-0.1875, 0.125, 0);
        else if (isOffset)
            matrices.translate(-0.1875, 0.1875, 0);
        else
            matrices.translate(-0.0625, 0.0625, 0);

        if (FLIPPED_OFFSET.contains(projectile))
            matrices.scale(-1, 1, -1);

        if (OFFSET_ROTATE_CLOCKWISE.contains(projectile))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(-MathHelper.HALF_PI));
        else if (OFFSET_ROTATE_COUNTERCLOCKWISE.contains(projectile) || SHIFT_ROTATE_COUNTERCLOCKWISE.contains(projectile))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(MathHelper.HALF_PI));
        else if (!isOffset && !NO_ROTATE.contains(projectile))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(MathHelper.HALF_PI / 2));
    }

    public static boolean projectileNonBillboard(Item projectile) {
        return (OFFSET_NO_ROTATE.contains(projectile) && projectile != Items.COBWEB) || OFFSET_ROTATE_CLOCKWISE.contains(projectile) || OFFSET_ROTATE_COUNTERCLOCKWISE.contains(projectile) || SHIFT_ROTATE_COUNTERCLOCKWISE.contains(projectile);
    }
}
