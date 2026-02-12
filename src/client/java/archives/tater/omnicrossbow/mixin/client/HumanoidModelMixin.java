package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.OmniCrossbowClient;

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
        var spinning = state.getDataOrDefault(OmniCrossbowClient.SPINNING_ITEM, false);
        var otherHandNotPosed = offhand || spinning;

        original.call(
                otherHandNotPosed && !holdingInRightArm ? DUMMY : rightArm,
                otherHandNotPosed && holdingInRightArm ? DUMMY : leftArm,
                head,
                holdingInRightArm
        );
    }
}
