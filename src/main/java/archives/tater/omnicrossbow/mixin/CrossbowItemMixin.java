package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.OmniEnchantment;
import archives.tater.omnicrossbow.util.OmniUtil;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Shadow
    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        throw new AssertionError();
    }

    // --- OMNI ---

    @Inject(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"),
            cancellable = true
    )
    private static void customProjectile(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci) {
        if (!OmniEnchantment.shootProjectile((ServerWorld) world, shooter, crossbow, projectile)) return;
        ci.cancel();
        if (OmniEnchantment.shouldUnloadImmediate(projectile))
            crossbow.damage(1, shooter, e -> e.sendToolBreakStatus(hand));
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), OmniEnchantment.getSound(projectile), SoundCategory.PLAYERS, 1.0F, soundPitch);
    }

    @WrapWithCondition(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;setCharged(Lnet/minecraft/item/ItemStack;Z)V")
    )
    private boolean preventUncharge(ItemStack stack, boolean charged) {
        return OmniEnchantment.shouldUnloadImmediate(OmniUtil.getMainProjectile(stack));
    }

    @WrapWithCondition(
            method = "postShoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;clearProjectiles(Lnet/minecraft/item/ItemStack;)V")
    )
    private static boolean preventClear(ItemStack crossbow) {
        return OmniEnchantment.shouldUnloadImmediate(OmniUtil.getMainProjectile(crossbow));
    }

    @Inject(
            method = "postShoot",
            at = @At("HEAD")
    )
    private static void ejectRemainder(World world, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        if (world.isClient) return;
        for (var projectile : getProjectiles(stack)) {
            var remainder = OmniEnchantment.getRemainder(projectile);
            if (remainder.isEmpty()) continue;
            entity.dropStack(remainder, entity.getEyeHeight(entity.getPose()) - 0.1f);
        }
    }

    // --- MULTICHAMBERED ---

    @ModifyExpressionValue(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;getProjectiles(Lnet/minecraft/item/ItemStack;)Ljava/util/List;")
    )
    private static List<ItemStack> chooseOne(List<ItemStack> original, @Local(argsOnly = true) ItemStack crossbow) {
        if (original.isEmpty() || !MultichamberedEnchantment.hasMultichambered(crossbow)) return original;
        return List.of(original.get(0));
    }

    @WrapWithCondition(
            method = "clearProjectiles",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;clear()V")
    )
    private static boolean clearOne(NbtList instance, @Local(argsOnly = true) ItemStack crossbow) {
        if (instance.isEmpty() || !MultichamberedEnchantment.hasMultichambered(crossbow)) return true;
        instance.remove(0);
        return false;
    }

    @ModifyConstant(
            method = "clearProjectiles",
            constant = @Constant(intValue = NbtElement.LIST_TYPE)
    )
    private static int fixClear(int constant) {
        // The vanilla code isn't even set up correctly...
        return NbtElement.COMPOUND_TYPE;
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z", ordinal = 0)
    )
    private boolean checkCrouch(ItemStack stack, Operation<Boolean> original, @Local(argsOnly = true) PlayerEntity user) {
        return original.call(stack) && !(MultichamberedEnchantment.hasMultichambered(stack) && user.isSneaking());
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z", ordinal = 1)
    )
    private boolean checkNumLoaded(ItemStack stack, Operation<Boolean> original) {
        if (!original.call(stack)) return false;
        // I think IntelliJ was bugging here
        //noinspection ConstantValue
        return !MultichamberedEnchantment.hasMultichambered(stack) || getProjectiles(stack).size() >= MultichamberedEnchantment.getMaxShots(EnchantmentHelper.getLevel(OmniCrossbow.MULTICHAMBERED, stack));
    }

    @WrapWithCondition(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;setCharged(Lnet/minecraft/item/ItemStack;Z)V")
    )
    private boolean preventUnload(ItemStack stack, boolean charged) {
        return getProjectiles(stack).size() <= 0;
    }

    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean checkNumLoaded2(ItemStack stack, Operation<Boolean> original) {
        if (!original.call(stack)) return false;
        //noinspection ConstantValue
        return !MultichamberedEnchantment.hasMultichambered(stack) || getProjectiles(stack).size() >= MultichamberedEnchantment.getMaxShots(EnchantmentHelper.getLevel(OmniCrossbow.MULTICHAMBERED, stack));
    }

    // loadProjectile already appends to projectiles

    @Inject(
            method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0),
            cancellable = true
    )
    private void multichamberedTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci, @Local(ordinal = 1) List<ItemStack> projectiles) {
        if (!MultichamberedEnchantment.hasMultichambered(stack)) return;

        tooltip.add(Text.translatable("item.minecraft.crossbow.projectile"));
        for (var projectile : projectiles) {
            tooltip.add(Text.literal("  ").append(projectile.toHoverableText()));
            if (context.isAdvanced() && projectile.isOf(Items.FIREWORK_ROCKET)) {
                List<Text> fireworkTooltip = Lists.<Text>newArrayList();
                Items.FIREWORK_ROCKET.appendTooltip(projectile, world, fireworkTooltip, context);
                if (!fireworkTooltip.isEmpty()) {
                    fireworkTooltip.replaceAll(text -> Text.literal("    ").append(text).formatted(Formatting.GRAY));

                    tooltip.addAll(fireworkTooltip);
                }
            }
        }

        ci.cancel();
    }

}
