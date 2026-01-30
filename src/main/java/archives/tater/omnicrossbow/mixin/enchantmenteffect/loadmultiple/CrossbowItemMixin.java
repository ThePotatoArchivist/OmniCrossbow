package archives.tater.omnicrossbow.mixin.enchantmenteffect.loadmultiple;

import archives.tater.omnicrossbow.enchantment.LoadMultiple;
import archives.tater.omnicrossbow.registry.OmniCrossbowComponents;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ChargedProjectiles;isEmpty()Z")
    )
    // Returns true: Load
    // Returns false: Shoot
    private boolean loadMultiple(ChargedProjectiles instance, Operation<Boolean> original, @Local(name = "itemStack") ItemStack stack, @Local(argsOnly = true) Player player) {
        if (original.call(instance)) return true;
        if (!player.isSecondaryUseActive()) return false;

        return instance.items().size() < LoadMultiple.maxProjectilesOrDefault(stack);
    }

    @Inject(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startUsingItem(Lnet/minecraft/world/InteractionHand;)V")
    )
    private void saveProjectiles(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        var stack = player.getItemInHand(hand);
        stack.set(OmniCrossbowComponents.ADDITIONAL_CHARGED_PROJECTILES, stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY));
    }

    @WrapOperation(
            method = "tryLoadProjectiles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private static <T> T mergeProjectiles(ItemStack instance, DataComponentType<T> type, @Nullable T value, Operation<T> original) {
        if (type != DataComponents.CHARGED_PROJECTILES || value == null) return original.call(instance, type, value);

        var existing = instance.get(OmniCrossbowComponents.ADDITIONAL_CHARGED_PROJECTILES);
        if (existing == null) return original.call(instance, type, value);

        return original.call(instance, type, new ChargedProjectiles(Stream.concat(
                existing.items().stream(),
                ((ChargedProjectiles) value).items().stream()
        ).toList()));
    }

    @Inject(
            method = "releaseUsing",
            at = @At("TAIL")
    )
    private void fixAdditional(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime, CallbackInfoReturnable<Boolean> cir) {
        var additional = itemStack.remove(OmniCrossbowComponents.ADDITIONAL_CHARGED_PROJECTILES);
        var chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty())
            itemStack.set(DataComponents.CHARGED_PROJECTILES, additional);
    }
}
