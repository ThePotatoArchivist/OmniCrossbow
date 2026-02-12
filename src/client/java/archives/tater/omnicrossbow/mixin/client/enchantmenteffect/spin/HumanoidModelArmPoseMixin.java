package archives.tater.omnicrossbow.mixin.client.enchantmenteffect.spin;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(HumanoidModel.ArmPose.class)
public class HumanoidModelArmPoseMixin {
    @Inject(
            method = "animateUseItem",
            at = @At("HEAD")
    )
    private <S extends ArmedEntityRenderState> void spinCrossbow(S state, PoseStack poseStack, float ticksUsingItem, HumanoidArm arm, ItemStack actualItem, CallbackInfo ci) {
        if (state.getDataOrDefault(OmniCrossbowClient.SPINNING_ITEM, false))
            OmniCrossbowClient.transformCrossbowSpinModel(poseStack, ticksUsingItem, arm == HumanoidArm.LEFT);
    }
}
