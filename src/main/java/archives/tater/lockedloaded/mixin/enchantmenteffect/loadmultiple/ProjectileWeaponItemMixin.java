package archives.tater.lockedloaded.mixin.enchantmenteffect.loadmultiple;

import archives.tater.lockedloaded.enchantment.LoadMultiple;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;

import static java.lang.Math.min;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @WrapOperation(
            method = "draw",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processProjectileCount(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;I)I")
    )
    private static int limitProjectileCount(ServerLevel serverLevel, ItemStack weapon, Entity shooter, int count, Operation<Integer> original) {
        var result = original.call(serverLevel, weapon, shooter, count);

        var maxProjectiles = LoadMultiple.maxProjectiles(weapon);
        if (maxProjectiles == null) return result;

        var chargedCount = weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).items().size();
        return min(result, maxProjectiles - chargedCount);
    }
}
