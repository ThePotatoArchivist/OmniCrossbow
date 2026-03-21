package archives.tater.lockedloaded.mixin.enchantmenteffect.firedcount;

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.Nullable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "performShooting",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private <T> T firedCount(ItemStack instance, DataComponentType<T> type, @Nullable T value, Operation<T> original, @Local(name = "serverLevel") ServerLevel serverLevel, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter) {
        if (type != DataComponents.CHARGED_PROJECTILES) return original.call(instance, type, value);

        var items = instance.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).items();

        var dirtyCount = new MutableFloat(items.size());
        EnchantmentHelper.runIterationOnItem(instance, (enchantment, level) ->
                enchantment.value().modifyEntityFilteredValue(
                        LockedLoadedEnchantmentEffects.PROJECTILE_FIRED_COUNT,
                        serverLevel,
                        level,
                        instance,
                        shooter,
                        dirtyCount
                )
        );
        var count = dirtyCount.intValue();

        // If not trying to shoot less, use base logic
        if (count >= items.size()) return original.call(instance, type, value);

        // Set to remainder
        original.call(instance, type, new ChargedProjectiles(items.subList(count, items.size())));

        // Return head
        return (T) new ChargedProjectiles(items.subList(0, count));
    }
}
