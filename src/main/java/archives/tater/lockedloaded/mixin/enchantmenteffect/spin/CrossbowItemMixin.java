package archives.tater.lockedloaded.mixin.enchantmenteffect.spin;

import archives.tater.lockedloaded.registry.LockedLoadedAttachments;
import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;
import archives.tater.lockedloaded.registry.LockedLoadedSounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import static archives.tater.lockedloaded.util.OmniUtil.getFirstEnchantmentComponent;
import static java.util.Objects.requireNonNullElse;

@SuppressWarnings("UnstableApiUsage")
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Shadow
    public abstract void performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float power, float uncertainty, @Nullable LivingEntity targetOverride);

    @Shadow
    private static float getShootingPower(ChargedProjectiles projectiles) {
        return 0;
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;performShooting(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;FFLnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void startSpin(CrossbowItem instance, Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float power, float uncertainty, LivingEntity targetOverride, Operation<Void> original) {
        if (!EnchantmentHelper.has(weapon, LockedLoadedEnchantmentEffects.CROSSBOW_SPIN)) {
            original.call(instance, level, shooter, hand, weapon, power, uncertainty, targetOverride);
            return;
        }
        shooter.setAttached(LockedLoadedAttachments.SPINNING_ITEM, Unit.INSTANCE);
        shooter.startUsingItem(hand);

        // TODO lockedloaded play charge delayed charge sound
    }

    @Inject(
            method = "onUseTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tickSpinning(Level level, LivingEntity entity, ItemStack itemStack, int ticksRemaining, CallbackInfo ci) {
        if (!entity.hasAttached(LockedLoadedAttachments.SPINNING_ITEM)) return;

        if (entity.getTicksUsingItem() % 27 == 0)
            entity.playSound(LockedLoadedSounds.SPIN);

        ci.cancel();
    }

    @Inject(
            method = "releaseUsing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void releaseSpin(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime, CallbackInfoReturnable<Boolean> cir) {
        if (!entity.hasAttached(LockedLoadedAttachments.SPINNING_ITEM)) return;

        var chargedProjectiles = itemStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (chargedProjectiles.isEmpty()) return;

        float minSpinTicks = requireNonNullElse(getFirstEnchantmentComponent(itemStack, LockedLoadedEnchantmentEffects.CROSSBOW_SPIN, LevelBasedValue::calculate), 0f);
        if (entity.getTicksUsingItem() > minSpinTicks) {
            performShooting(level, entity, entity.getUsedItemHand(), itemStack, getShootingPower(chargedProjectiles), 1.0F, null);

            // Shoot other hand crossbow
            var otherHand = switch (entity.getUsedItemHand()) {
                case OFF_HAND -> InteractionHand.MAIN_HAND;
                case MAIN_HAND -> InteractionHand.OFF_HAND;
            };
            var otherHandStack = entity.getItemInHand(otherHand);
            if (otherHandStack.getItem() instanceof CrossbowItem crossbowItem && EnchantmentHelper.has(otherHandStack, LockedLoadedEnchantmentEffects.CROSSBOW_SPIN)) {
                var otherProjectiles = otherHandStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
                if (!otherProjectiles.isEmpty())
                    crossbowItem.performShooting(level, entity, otherHand, otherHandStack, getShootingPower(otherProjectiles), 1.0f, null);
            }
        }

        cir.setReturnValue(true);
    }
}
