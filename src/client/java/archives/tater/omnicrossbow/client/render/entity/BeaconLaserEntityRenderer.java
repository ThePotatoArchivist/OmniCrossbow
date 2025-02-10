package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.entity.BeaconLaserEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import static archives.tater.omnicrossbow.entity.BeaconLaserEntity.MAX_FIRING_TICKS;

@Environment(EnvType.CLIENT)
public class BeaconLaserEntityRenderer extends EntityRenderer<BeaconLaserEntity> {
    public static final Identifier BEAM_TEXTURE = BeaconBlockEntityRenderer.BEAM_TEXTURE;
    public static final int TRANSITION = 6;

    public BeaconLaserEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    protected int getBlockLight(BeaconLaserEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(BeaconLaserEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.getFiringTicks() <= 0) return;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw())));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) + 90));
        var firingTime = entity.getFiringTicks() - 1 + tickDelta;
        var beamWidthScale = trapezoidalTransition(firingTime);
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.floorMod(5 * firingTime, 90)));
        matrices.translate(-0.5f, 0, -0.5f); // Beacon expects to start at corner
        BeaconBlockEntityRenderer.renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0f, entity.getWorld().getTime(), 0, (int) entity.getDistance(), DyeColor.WHITE.getEntityColor(), 0.2f * beamWidthScale, 0.25f * beamWidthScale * (1 + 0.2f * MathHelper.sin(0.7f * firingTime)));
        matrices.pop();
    }

    public float trapezoidalTransition(float progress) {
        if (progress <= TRANSITION) {
            return progress / TRANSITION;
        } if (progress >= MAX_FIRING_TICKS - TRANSITION) {
            return (MAX_FIRING_TICKS - progress) / TRANSITION;
        } else {
            return 1;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identifier getTexture(BeaconLaserEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
