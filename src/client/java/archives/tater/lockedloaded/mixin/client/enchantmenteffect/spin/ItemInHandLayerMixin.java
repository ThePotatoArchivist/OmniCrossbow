package archives.tater.lockedloaded.mixin.client.enchantmenteffect.spin;

import archives.tater.lockedloaded.client.render.CrossbowSpinRendering;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Inject(
            method = "submitArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V")
    )
    private <S extends ArmedEntityRenderState> void spinCrossbow(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (!state.getDataOrDefault(CrossbowSpinRendering.SPINNING_ITEM, false)) return;
        var ticksUsingItem = state.ticksUsingItem(arm);
        if (ticksUsingItem == 0 && !CrossbowSpinRendering.shouldSpin(itemStack)) return;
        var animateTicks = ticksUsingItem == 0 ? state.ticksUsingItem(arm.getOpposite()) : ticksUsingItem;
        CrossbowSpinRendering.transformCrossbowSpinModel(poseStack, animateTicks, arm == HumanoidArm.LEFT);
    }
}
