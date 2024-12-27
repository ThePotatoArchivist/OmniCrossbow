package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(HostileEntity.class)
public class HostileEntityMixin {
    @ModifyExpressionValue(
            method = "getProjectileType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectiles()Ljava/util/function/Predicate;")
    )
    private Predicate<ItemStack> allowAnything(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack itemStack) {
        if (itemStack.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(OmniCrossbow.OMNI, itemStack) > 0) {
            return stack -> !stack.isEmpty() && stack != itemStack;
        }
        return original;
    }
}
