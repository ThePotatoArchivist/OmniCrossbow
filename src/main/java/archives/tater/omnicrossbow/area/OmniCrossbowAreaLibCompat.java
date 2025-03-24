package archives.tater.omnicrossbow.area;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OmniCrossbowAreaLibCompat {

    public static final AreaDataComponentType<BlocksModifiableComponent> BLOCKS_MODIFIABLE =
            AreaDataComponentTypeRegistry.registerTracking(OmniCrossbow.id("blocks_modifiable"), BlocksModifiableComponent::new);

    public static final AreaDataComponentType<TemporaryBlocksPlaceableComponent> TEMPORARY_BLOCKS_PLACEABLE =
            AreaDataComponentTypeRegistry.registerTracking(OmniCrossbow.id("temporary_blocks_placeable"), TemporaryBlocksPlaceableComponent::new);

    private static final String AREA_ARG = "area";
    private static final String ALLOWED_ARG = "allowed";

    private static <T extends AreaDataComponent> int runAreaCommand(CommandContext<ServerCommandSource> context, Area area, AreaDataComponentType<T> type, boolean allowed) {
        if (allowed)
            area.put(context.getSource().getServer(), type, type.factory().get());
        else
            area.remove(context.getSource().getServer(), type);
        context.getSource().sendFeedback(() -> Text.translatable("command.omnicrossbow.modifiable_area", area.getId().toString(), allowed ? "true" : "false"), false);
        return allowed ? 1 : 0;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("omnicrossbow_area")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(argument(AREA_ARG, AreaArgument.area())
                        .then(literal("modifiable")
                            .then(argument(ALLOWED_ARG, BoolArgumentType.bool())
                                .executes(command -> runAreaCommand(
                                        command,
                                        AreaArgument.getArea(command, AREA_ARG),
                                        BLOCKS_MODIFIABLE,
                                        BoolArgumentType.getBool(command, ALLOWED_ARG)
                                )))
                            .executes(command -> runAreaCommand(
                                    command,
                                    AreaArgument.getArea(command, AREA_ARG),
                                    BLOCKS_MODIFIABLE,
                                    true
                            )))
                        .then(literal("temporary_placeable")
                                .then(argument(ALLOWED_ARG, BoolArgumentType.bool())
                                        .executes(command -> runAreaCommand(
                                                command,
                                                AreaArgument.getArea(command, AREA_ARG),
                                                TEMPORARY_BLOCKS_PLACEABLE,
                                                BoolArgumentType.getBool(command, ALLOWED_ARG)
                                        )))
                                .executes(command -> runAreaCommand(
                                        command,
                                        AreaArgument.getArea(command, AREA_ARG),
                                        TEMPORARY_BLOCKS_PLACEABLE,
                                        true
                                )))
            ));
        });
    }

    public static boolean containedInArea(@Nullable World world, BlockPos pos, AreaDataComponentType<?> type) {
        return world != null && world.getServer() != null &&
                AreaSavedData.getServerData(world.getServer())
                        .findTrackedAreasContaining(world, pos.toCenterPos()).stream()
                        .anyMatch(area -> area.has(type));
    }

    public static boolean containedInModifiableArea(@Nullable World world, BlockPos pos) {
        return containedInArea(world, pos, OmniCrossbowAreaLibCompat.BLOCKS_MODIFIABLE);
    }
}
