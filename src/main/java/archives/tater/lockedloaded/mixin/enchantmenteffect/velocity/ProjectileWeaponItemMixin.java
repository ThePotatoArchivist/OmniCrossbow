package archives.tater.lockedloaded.mixin.enchantmenteffect.velocity;

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.apache.commons.lang3.mutable.MutableFloat;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @ModifyVariable(
            method = "shoot",
            at = @At("HEAD"),
            name = "power"
    )
    private float modifyVelocity(float power, @Local(argsOnly = true) ItemStack weapon, @Local(argsOnly = true) ServerLevel serverLevel) {
        if (!EnchantmentHelper.has(weapon, LockedLoadedEnchantmentEffects.PROJECTILE_VELOCITY)) return power;
        var value = new MutableFloat(power);
        EnchantmentHelper.runIterationOnItem(weapon, (enchantment, level) ->
                enchantment.value().modifyItemFilteredCount(LockedLoadedEnchantmentEffects.PROJECTILE_VELOCITY, serverLevel, level, weapon, value)
        );
        return value.floatValue();
    }
}
