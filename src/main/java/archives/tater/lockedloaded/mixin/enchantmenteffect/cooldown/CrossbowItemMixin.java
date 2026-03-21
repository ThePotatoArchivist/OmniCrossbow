package archives.tater.lockedloaded.mixin.enchantmenteffect.cooldown;

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import org.apache.commons.lang3.mutable.MutableFloat;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(
            method = "performShooting",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER)
    )
    private void cooldown(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float power, float uncertainty, LivingEntity targetOverride, CallbackInfo ci) {
        if (!(shooter instanceof ServerPlayer serverPlayer) || !EnchantmentHelper.has(weapon, LockedLoadedEnchantmentEffects.CROSSBOW_COOLDOWN)) return;
        var serverLevel = serverPlayer.level();

        var value = new MutableFloat(0);
        EnchantmentHelper.runIterationOnItem(weapon, (enchantment, enchantmentLevel) ->
                enchantment.value().modifyEntityFilteredValue(
                        LockedLoadedEnchantmentEffects.CROSSBOW_COOLDOWN,
                        serverLevel,
                        enchantmentLevel,
                        weapon,
                        shooter,
                        value
                )
        );

        if (value.floatValue() == 0) return;

        serverPlayer.getCooldowns().addCooldown(weapon, (int) (20 * value.floatValue()));
    }

}
