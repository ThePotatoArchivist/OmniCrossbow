package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbowEnchantmentEffects;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyExpressionValue(
            method = "getProjectileType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectiles()Ljava/util/function/Predicate;")
    )
    private Predicate<ItemStack> allowAnything(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack itemStack) {
        if (itemStack.getItem() instanceof CrossbowItem && EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, OmniCrossbowEnchantmentEffects.LOAD_ANY_ITEM)) {
            return stack -> !stack.isEmpty() && stack != itemStack;
        }
        return original;
    }
}
