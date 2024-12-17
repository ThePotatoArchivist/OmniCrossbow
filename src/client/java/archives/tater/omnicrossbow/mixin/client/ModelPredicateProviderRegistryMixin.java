package archives.tater.omnicrossbow.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelPredicateProviderRegistry.class)
public class ModelPredicateProviderRegistryMixin {
    @WrapOperation(
            method = "method_27886",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private static boolean checkPulling(ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity user) {
        return original.call(stack) && user.getActiveItem() != stack;
    }

    @WrapOperation(
            method = "method_27888",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private static boolean ignoreCharged(ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity user) {
        return user.getActiveItem() != stack;
    }

    @WrapOperation(
            method = "method_27887",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private static boolean ignoreCharged2(ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity user) {
        return false;
    }
}
