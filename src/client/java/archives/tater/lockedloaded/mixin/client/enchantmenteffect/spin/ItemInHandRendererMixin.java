package archives.tater.lockedloaded.mixin.client.enchantmenteffect.spin;

import archives.tater.lockedloaded.client.render.CrossbowSpinRendering;
import archives.tater.lockedloaded.registry.LockedLoadedAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @ModifyExpressionValue(
            method = "selectionUsingItemWhileHoldingBowLike",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean normalPoseSpin(boolean original, @Local(argsOnly = true) LocalPlayer player) {
        return original && !player.hasAttached(LockedLoadedAttachments.SPINNING_ITEM);
    }

    @WrapOperation(
            method = "selectionUsingItemWhileHoldingBowLike",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;isChargedCrossbow(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private static boolean showBothCrossbows(ItemStack item, Operation<Boolean> original, @Local(argsOnly = true) LocalPlayer player) {
        return original.call(item) && !(CrossbowSpinRendering.shouldSpin(item) && CrossbowSpinRendering.shouldSpin(player.getMainHandItem()));
    }

    @ModifyExpressionValue(
            method = "renderArmWithItem",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z")
            }
    )
    private static boolean normalPoseSpin(boolean original, @Local(argsOnly = true) AbstractClientPlayer player) {
        return original && !player.hasAttached(LockedLoadedAttachments.SPINNING_ITEM);
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V")
    )
    private void spin(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (!player.hasAttached(LockedLoadedAttachments.SPINNING_ITEM) || player.getUsedItemHand() != hand && !CrossbowSpinRendering.shouldSpin(itemStack)) return;

        CrossbowSpinRendering.transformCrossbowSpinInHand(poseStack, player.getTicksUsingItem(frameInterp), player.getMainArm() == HumanoidArm.LEFT ^ hand == InteractionHand.OFF_HAND);
    }
}
