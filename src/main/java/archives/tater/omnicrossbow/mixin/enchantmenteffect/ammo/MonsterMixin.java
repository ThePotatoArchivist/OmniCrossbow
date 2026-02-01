package archives.tater.omnicrossbow.mixin.enchantmenteffect.ammo;

import archives.tater.omnicrossbow.enchantment.Ammo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

@Mixin(Monster.class)
public class MonsterMixin {
    @ModifyExpressionValue(
            method = "getProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles()Ljava/util/function/Predicate;")
    )
    private Predicate<ItemStack> ammoEnchantment(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack weapon) {
        return original.or(Ammo.supportedProjectiles(weapon, true));
    }
}
