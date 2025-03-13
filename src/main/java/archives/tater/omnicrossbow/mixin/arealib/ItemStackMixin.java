package archives.tater.omnicrossbow.mixin.arealib;

import archives.tater.omnicrossbow.area.OmniCrossbowAreaLibCompat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @ModifyExpressionValue(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;canPlaceOn(Lnet/minecraft/block/pattern/CachedBlockPosition;)Z")
    )
    private boolean allowInArea(boolean original, @Local(argsOnly = true) ItemUsageContext context) {
        return original || OmniCrossbowAreaLibCompat.containedInModifiableArea(context.getWorld(), context.getBlockPos().offset(context.getSide()));
    }
}
