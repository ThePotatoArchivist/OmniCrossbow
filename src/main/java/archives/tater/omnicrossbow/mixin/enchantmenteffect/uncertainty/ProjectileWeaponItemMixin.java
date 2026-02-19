package archives.tater.omnicrossbow.mixin.enchantmenteffect.uncertainty;

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @ModifyVariable(
            method = "shoot",
            at = @At("HEAD"),
            argsOnly = true,
            name = "uncertainty"
    )
    private float modifyUncertainty(float original, @Local(argsOnly = true) ItemStack weapon, @Local(argsOnly = true) ServerLevel serverLevel, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Local(argsOnly = true) List<ItemStack> projectiles) {
        if (!EnchantmentHelper.has(weapon, OmniCrossbowEnchantmentEffects.PROJECTILE_UNCERTAINTY)) return original;

        var projectileCount = projectiles.size();
        var value = new MutableFloat(original);

        EnchantmentHelper.runIterationOnItem(weapon, (enchantment, level) ->
                Enchantment.applyEffects(
                        enchantment.value().getEffects(OmniCrossbowEnchantmentEffects.PROJECTILE_UNCERTAINTY),
                        Enchantment.entityContext(serverLevel, level, shooter, shooter.position()),
                        e -> value.setValue(e.process(level, projectileCount, shooter.getRandom(), value.floatValue()))
                )
        );

        return value.floatValue();
    }
}
