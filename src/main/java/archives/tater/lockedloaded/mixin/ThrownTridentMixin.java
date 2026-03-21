package archives.tater.lockedloaded.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
    protected ThrownTridentMixin(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
    }

    @WrapOperation(
            method = "tryPickup",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean preventPickup(Inventory instance, ItemStack itemStack, Operation<Boolean> original) {
        return pickup == Pickup.CREATIVE_ONLY || original.call(instance, itemStack);
    }
}
