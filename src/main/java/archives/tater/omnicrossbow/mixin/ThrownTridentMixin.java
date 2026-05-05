package archives.tater.omnicrossbow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends AbstractArrow {
    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> ID_FOIL;

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

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void fixFoil(ValueInput input, CallbackInfo ci) {
        entityData.set(ID_FOIL, getPickupItem().hasFoil());
    }
}
