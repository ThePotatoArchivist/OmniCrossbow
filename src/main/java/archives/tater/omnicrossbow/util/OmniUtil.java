package archives.tater.omnicrossbow.util;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.area.AreaCheckExplosionBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Most of these should probably be statically imported
 */
public class OmniUtil {
    public static ItemStack getMainProjectile(ItemStack crossbow) {
        var projectiles = crossbow.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT).getProjectiles();
        return projectiles.isEmpty() ? ItemStack.EMPTY : projectiles.getFirst();
    }

    public static boolean modifyNotRestrictedAt(World world, @Nullable Entity entity, BlockPos pos) {
        return !(entity instanceof ServerPlayerEntity serverPlayer) || modifyNotRestrictedAt(world, serverPlayer, pos);
    }

    public static boolean modifyNotRestrictedAt(World world, ServerPlayerEntity player, BlockPos pos) {
        return !player.isBlockBreakingRestricted(world, pos, player.interactionManager.getGameMode());
    }

    public static boolean canModifyAt(World world, @Nullable Entity entity, BlockPos pos) {
        return entity instanceof ServerPlayerEntity serverPlayer && canModifyAt(world, serverPlayer, pos);
    }

    public static boolean canModifyAt(World world, ServerPlayerEntity player, BlockPos pos) {
        return player.canModifyAt(world, pos) && modifyNotRestrictedAt(world, player, pos);
    }

    public static void areaCheckExplosion(World world, Entity source, Entity owner, float power) {
        world.createExplosion(source,
                Explosion.createDamageSource(world, source),
                OmniCrossbow.AREALIB_INSTALLED
                        && owner instanceof ServerPlayerEntity player
                        && player.interactionManager.getGameMode().isBlockBreakingRestricted()
                        ? new AreaCheckExplosionBehavior(null) : null,
                source.getX(),
                source.getBodyY(0.5),
                source.getZ(),
                power,
                true,
                World.ExplosionSourceType.BLOCK);
    }

    public static <K1, V1, K2, V2> Map<K2, V2> map(Map<K1, V1> map, BiFunction<K1, V1, K2> keySelector, BiFunction<K1, V1, V2> valueSelector) {
        return map.entrySet().stream().collect(Collectors.toMap(entry -> keySelector.apply(entry.getKey(), entry.getValue()), entry -> valueSelector.apply(entry.getKey(), entry.getValue())));
    }

    public static <K1, V1, T> Stream<T> map(Map<K1, V1> map, BiFunction<K1, V1, T> transform) {
        return map.entrySet().stream().map(entry -> transform.apply(entry.getKey(), entry.getValue()));
    }
}
