package archives.tater.lockedloaded.mixin.client.enchantmenteffect.spin;

import archives.tater.lockedloaded.client.render.CrossbowSpinRendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;

import java.util.List;
import java.util.Map;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Unique
    private static final ModelPart DUMMY = new ModelPart(List.of(), Map.of());

    @WrapOperation(
            method = {
                    "poseLeftArm",
                    "poseRightArm"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateCrossbowHold(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Z)V")
    )
    private <T extends HumanoidRenderState> void noHoldArm(ModelPart rightArm, ModelPart leftArm, ModelPart head, boolean holdingInRightArm, Operation<Void> original, @Local(argsOnly = true) T state) {
        var isRightHanded = state.mainArm == HumanoidArm.RIGHT;
        var offhand = holdingInRightArm ^ isRightHanded;
        boolean spinning = state.getDataOrDefault(CrossbowSpinRendering.SPINNING_ITEM, false);
        var otherHandNotPosed = offhand || spinning || (CrossbowItem.isCharged(state.leftHandItemStack) && CrossbowItem.isCharged(state.rightHandItemStack));

        original.call(
                otherHandNotPosed && !holdingInRightArm ? DUMMY : rightArm,
                otherHandNotPosed && holdingInRightArm ? DUMMY : leftArm,
                head,
                holdingInRightArm
        );
        if (spinning)
            CrossbowSpinRendering.transformCrossbowSpinTilt(holdingInRightArm ? rightArm : leftArm, !holdingInRightArm);
    }

    @WrapOperation(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;affectsOffhandPose()Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;isTwoHanded()Z")
            }
    )
    private <T extends HumanoidRenderState> boolean twoHandedCrossbow(HumanoidModel.ArmPose instance, Operation<Boolean> original, @Local(argsOnly = true) T state) {
        if (!original.call(instance)) return false;
        if (instance != HumanoidModel.ArmPose.CROSSBOW_HOLD) return true;

        return !CrossbowItem.isCharged(state.leftHandItemStack) || !CrossbowItem.isCharged(state.rightHandItemStack);
    }
}
