package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.OmniCrossbowEnchantmentEffects;
import archives.tater.omnicrossbow.OmniEnchantment;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    // --- OMNI ---

    @Shadow protected abstract ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical);

    @Inject(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER)
    )
    private void ejectRemainder(World world, LivingEntity shooter, Hand hand, ItemStack stack, float speed, float divergence, LivingEntity target, CallbackInfo ci, @Local ChargedProjectilesComponent chargedProjectilesComponent) {
        for (var projectile : chargedProjectilesComponent.getProjectiles()) {
            var remainder = OmniEnchantment.getRemainder(projectile);
            if (remainder.isEmpty()) continue;
            shooter.dropStack(remainder, shooter.getEyeHeight(shooter.getPose()) - 0.1f);
        }
    }

    // --- BOTH ---

    @SuppressWarnings("MixinExtrasOperationParameters")
    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private <T> @Nullable T preventUncharge(ItemStack instance, ComponentType<T> type, @Nullable T value, Operation<T> original) {
        var component = instance.get(type);
        if (!(component instanceof ChargedProjectilesComponent projectilesComponent) || projectilesComponent.isEmpty()) return original.call(instance, type, value); // sanity check
        if (!OmniEnchantment.shouldUnloadImmediate(projectilesComponent.getProjectiles().getFirst())) return component;
        if (EnchantmentHelper.hasAnyEnchantmentsWith(instance, OmniCrossbowEnchantmentEffects.ONE_PROJECTILE_AT_TIME) && projectilesComponent.getProjectiles().size() > 1) {
            MultichamberedEnchantment.unloadOneProjectile(instance);
            return component;
        }
        return original.call(instance, type, value);
    }

    // --- MULTICHAMBERED ---

    @ModifyExpressionValue(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/ChargedProjectilesComponent;getProjectiles()Ljava/util/List;")
    )
    private List<ItemStack> chooseOne(List<ItemStack> original, @Local(argsOnly = true) ItemStack crossbow, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Local ServerWorld serverWorld) {
        if (crossbow.contains(OmniCrossbow.CROSSBOW_FULL)) crossbow.remove(OmniCrossbow.CROSSBOW_FULL);
        if (original.isEmpty() || !MultichamberedEnchantment.hasMultichambered(crossbow)) return original;
        return List.of(original.getFirst());
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/ChargedProjectilesComponent;isEmpty()Z")
    )
    private boolean checkCrouch(ChargedProjectilesComponent instance, Operation<Boolean> original, @Local ItemStack crossbow, @Local(argsOnly = true) PlayerEntity user) {
        return original.call(instance) || ((MultichamberedEnchantment.hasMultichambered(crossbow) && user.isSneaking()));
    }

    @ModifyExpressionValue(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z")
    )
    private boolean checkNumLoaded(boolean original, @Local ItemStack stack) {
        return original || MultichamberedEnchantment.cannotLoadMore(stack);
    }

    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean allowIfMultichambered(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) && (stack.contains(OmniCrossbow.CROSSBOW_FULL) || !EnchantmentHelper.hasAnyEnchantmentsWith(stack, OmniCrossbowEnchantmentEffects.ONE_PROJECTILE_AT_TIME));
    }

    @ModifyExpressionValue(
            method = "loadProjectiles",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    private static boolean allowIfMultichambered(boolean original, @Local(argsOnly = true) ItemStack crossbow) {
        return original && (crossbow.contains(OmniCrossbow.CROSSBOW_FULL) || !EnchantmentHelper.hasAnyEnchantmentsWith(crossbow, OmniCrossbowEnchantmentEffects.ONE_PROJECTILE_AT_TIME));
    }

    @Inject(
            method = "loadProjectiles",
            at = @At(value = "RETURN", ordinal = 0)
    )
    private static void setFull(LivingEntity shooter, ItemStack crossbow, CallbackInfoReturnable<Boolean> cir, @Local List<ItemStack> list) {
        if (shooter.getWorld() instanceof ServerWorld serverWorld && list.size() >= EnchantmentHelper.getProjectileCount(serverWorld, crossbow, shooter, 1)) {
            crossbow.set(OmniCrossbow.CROSSBOW_FULL, Unit.INSTANCE);
        }
    }

    @Inject(
            method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/ChargedProjectilesComponent;getProjectiles()Ljava/util/List;"),
            cancellable = true
    )
    private void multichamberedTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci, @Local ChargedProjectilesComponent projectiles) {
        if (!MultichamberedEnchantment.hasMultichambered(stack)) return;

        tooltip.add(Text.translatable("item.minecraft.crossbow.projectile"));
        for (var projectile : projectiles.getProjectiles()) {
            tooltip.add(Text.literal("  ").append(projectile.toHoverableText()));
            if (type.isAdvanced() && projectile.isOf(Items.FIREWORK_ROCKET)) {
                List<Text> fireworkTooltip = Lists.<Text>newArrayList();
                Items.FIREWORK_ROCKET.appendTooltip(projectile, context, fireworkTooltip, type);
                if (!fireworkTooltip.isEmpty()) {
                    fireworkTooltip.replaceAll(text -> Text.literal("    ").append(text).formatted(Formatting.GRAY));

                    tooltip.addAll(fireworkTooltip);
                }
            }
        }

        ci.cancel();
    }

}
