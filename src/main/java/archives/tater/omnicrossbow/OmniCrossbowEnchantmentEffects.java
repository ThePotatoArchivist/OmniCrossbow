package archives.tater.omnicrossbow;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Unit;

import java.util.function.UnaryOperator;

public class OmniCrossbowEnchantmentEffects {
    private static <T> ComponentType<T> register(String path, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, OmniCrossbow.id(path), (builderOperator.apply(net.minecraft.component.ComponentType.builder())).build());
    }

    public static ComponentType<Unit> LOAD_ANY_ITEM = register("load_any_item", builder -> builder.codec(Unit.CODEC));

    public static ComponentType<Unit> ONE_PROJECTILE_AT_TIME = register("one_projectile_at_time", builder -> builder.codec(Unit.CODEC));

    public static void register() {}
}
