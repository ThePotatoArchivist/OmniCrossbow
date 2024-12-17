package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.entity.EmberEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class EmberEntityRenderer extends EntityRenderer<EmberEntity> {
    Identifier TEXTURE = new Identifier("textures/particle/lava.png");
    private final RenderLayer RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);

    public EmberEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EmberEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var buffer = vertexConsumers.getBuffer(RENDER_LAYER);
        matrices.push();
        matrices.translate(0, 0.125, 0);
        matrices.multiply(dispatcher.getRotation());
        var scale = 0.5f * entity.scale * (1 - (float) entity.getLandedTicks() / EmberEntity.MAX_LANDED_TICKS);
        matrices.scale(scale, scale, scale);
        var entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();
        vertex(buffer, positionMatrix, normalMatrix, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 0, 0, 1);
        vertex(buffer, positionMatrix, normalMatrix, LightmapTextureManager.MAX_LIGHT_COORDINATE, 1, 0, 1, 1);
        vertex(buffer, positionMatrix, normalMatrix, LightmapTextureManager.MAX_LIGHT_COORDINATE, 1, 1, 1, 0);
        vertex(buffer, positionMatrix, normalMatrix, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0, 1, 0, 0);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
        buffer.vertex(matrix, x - 0.5F, y - 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
                .next();
    }

    @Override
    public Identifier getTexture(EmberEntity entity) {
        return TEXTURE;
    }
}
