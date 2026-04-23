package archives.tater.omnicrossbow.mixin.enchantmenteffect.anyprojectile;

import archives.tater.omnicrossbow.registry.OmniCrossbowTags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

import org.jspecify.annotations.Nullable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @WrapOperation(
            method = "useAmmo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private static <T> @Nullable T nonIntangible(ItemStack instance, DataComponentType<T> type, @Nullable T value, Operation<T> original, ItemStack weapon, ItemStack projectile, LivingEntity holder) {
        return !holder.hasInfiniteMaterials() || projectile.is(OmniCrossbowTags.CREATIVE_INTANGIBLE_PROJECTILES) ? original.call(instance, type, value) : null;
    }
}
