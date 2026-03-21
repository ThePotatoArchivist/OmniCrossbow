package archives.tater.lockedloaded.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;

import org.jspecify.annotations.Nullable;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @Inject(
            method = "shoot",
            at = @At("HEAD")
    )
    private void saveCharged(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectiles, float power, float uncertainty, boolean isCrit, @Nullable LivingEntity targetOverride, CallbackInfo ci, @Share("chargedProjectiles") LocalRef<ChargedProjectiles> chargedProjectiles) {
        chargedProjectiles.set(weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY));
    }

    @Definition(id = "weapon", local = @Local(type = ItemStack.class, name = "weapon"))
    @Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    @Expression("weapon.isEmpty()")
    @ModifyExpressionValue(
            method = "shoot",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private boolean dropOtherProjectiles(boolean original, @Local(name = "i") int i, @Local(argsOnly = true) List<ItemStack> projectiles, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Share("chargedProjectiles") LocalRef<ChargedProjectiles> chargedProjectiles) {
        if (!original) return false;

        for (var j = i + 1; j < projectiles.size(); j++) {
            var projectile = projectiles.get(j);
            if (!projectile.has(DataComponents.INTANGIBLE_PROJECTILE))
                shooter.handleExtraItemsCreatedOnUse(projectile);
        }
        for (var projectile : chargedProjectiles.get().itemCopies())
            if (!projectile.has(DataComponents.INTANGIBLE_PROJECTILE))
                shooter.handleExtraItemsCreatedOnUse(projectile);

        return true;
    }
}
