package archives.tater.omnicrossbow.area;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.command.argument.AreaArgument;
import dev.doublekekse.area_lib.component.AreaDataComponentType;
import dev.doublekekse.area_lib.data.AreaSavedData;
import dev.doublekekse.area_lib.registry.AreaDataComponentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OmniCrossbowAreaLibCompat {

    public static final AreaDataComponentType<BlocksModifiableComponent> BLOCKS_MODIFIABLE =
            AreaDataComponentTypeRegistry.registerTracking(OmniCrossbow.id("blocks_modifiable"), BlocksModifiableComponent::new);

    private static final String AREA_ARG = "area";
    private static final String ALLOWED_ARG = "allowed";

    private static int runAreaCommand(CommandContext<ServerCommandSource> context, Area area, boolean allowed) {
        if (allowed)
            area.put(context.getSource().getServer(), BLOCKS_MODIFIABLE, new BlocksModifiableComponent());
        else
            area.remove(context.getSource().getServer(), BLOCKS_MODIFIABLE);
        context.getSource().sendFeedback(() -> Text.translatable("command.omnicrossbow.modifiable_area", area.getId().toString(), allowed ? "true" : "false"), false);
        return allowed ? 1 : 0;
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("omnicrossbow_modifiable_area")
                    .then(argument(AREA_ARG, AreaArgument.area())
                        .then(argument(ALLOWED_ARG, BoolArgumentType.bool())
                            .executes(command -> runAreaCommand(
                                    command,
                                    AreaArgument.getArea(command, AREA_ARG),
                                    BoolArgumentType.getBool(command, ALLOWED_ARG)
                            )))
                    .executes(command -> runAreaCommand(
                            command,
                            AreaArgument.getArea(command, AREA_ARG),
                            true
                    )))
            );
        });
    }

    public static boolean containedInModifiableArea(World world, BlockPos pos) {
        return world.getServer() != null &&
                AreaSavedData.getServerData(world.getServer())
                        .findTrackedAreasContaining(world, pos.toCenterPos()).stream()
                        .anyMatch(area -> area.has(BLOCKS_MODIFIABLE));
    }
}
