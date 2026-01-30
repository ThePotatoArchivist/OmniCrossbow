package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.registry.OmniCrossbowComponents;

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

@Mixin(Item.class)
public class ItemMixin {
    @Inject(
            method = "inventoryTick",
            at = @At("HEAD")
    )
    private void fixProjectiles(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci) {
        if (!(itemStack.getItem() instanceof CrossbowItem)) return;
        if (!(owner instanceof LivingEntity livingEntity) || livingEntity.isUsingItem()) return;
        var additional = itemStack.remove(OmniCrossbowComponents.ADDITIONAL_CHARGED_PROJECTILES);
        if (additional == null) return;
        var chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty())
            itemStack.set(DataComponents.CHARGED_PROJECTILES, additional);
    }
}
