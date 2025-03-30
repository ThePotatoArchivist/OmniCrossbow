package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingBobberEntityRenderer.class)
public abstract class FishingBobberEntityRendererMixin extends EntityRenderer<FishingBobberEntity> {
    protected FishingBobberEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/FishingBobberEntityRenderer;getHandPos(Lnet/minecraft/entity/player/PlayerEntity;FF)Lnet/minecraft/util/math/Vec3d;")
    )
    private Vec3d useCenterIfCrossbow(Vec3d original, @Local(argsOnly = true) FishingBobberEntity entity, @Local PlayerEntity player, @Local(argsOnly = true, ordinal = 1) float tickDelta) {
        if (entity instanceof GrappleFishingHookEntity && GrappleFishingHookEntity.hasFishingRodLoaded(player.getMainHandStack())) {
            double m = 960.0 / (double)dispatcher.gameOptions.getFov().getValue().intValue();
            Vec3d vec3d = dispatcher.camera.getProjection().getPosition(0, -0.3F).multiply(m);
            return player.getCameraPosVec(tickDelta).add(vec3d);
        }
        return original;
    }
}
