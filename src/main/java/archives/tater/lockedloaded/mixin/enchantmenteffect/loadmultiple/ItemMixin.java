package archives.tater.lockedloaded.mixin.enchantmenteffect.loadmultiple;

import archives.tater.lockedloaded.registry.LockedLoadedComponents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(
            method = "inventoryTick",
            at = @At("HEAD")
    )
    private void fixProjectiles(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
        if (!(itemStack.getItem() instanceof CrossbowItem)) return;
        if (!(owner instanceof LivingEntity livingEntity) || livingEntity.isUsingItem()) return;
        var additional = itemStack.remove(LockedLoadedComponents.ADDITIONAL_CHARGED_PROJECTILES);
        if (additional == null) return;
        if (itemStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty())
            itemStack.set(DataComponents.CHARGED_PROJECTILES, additional);
    }
}
