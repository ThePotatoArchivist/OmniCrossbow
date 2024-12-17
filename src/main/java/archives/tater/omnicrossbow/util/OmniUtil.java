package archives.tater.omnicrossbow.util;

import archives.tater.omnicrossbow.mixin.CrossbowItemInvoker;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Most of these should probably be statically imported
 */
public class OmniUtil {
    public static ItemStack getMainProjectile(ItemStack crossbow) {
        var projectiles = CrossbowItemInvoker.invokeGetProjectiles(crossbow);
        return projectiles.isEmpty() ? ItemStack.EMPTY : projectiles.get(0);
    }

    public static <K1, V1, K2, V2> Map<K2, V2> map(Map<K1, V1> map, BiFunction<K1, V1, K2> keySelector, BiFunction<K1, V1, V2> valueSelector) {
        return map.entrySet().stream().collect(Collectors.toMap(entry -> keySelector.apply(entry.getKey(), entry.getValue()), entry -> valueSelector.apply(entry.getKey(), entry.getValue())));
    }

    public static <K1, V1, T> Stream<T> map(Map<K1, V1> map, BiFunction<K1, V1, T> transform) {
        return map.entrySet().stream().map(entry -> transform.apply(entry.getKey(), entry.getValue()));
    }
}
