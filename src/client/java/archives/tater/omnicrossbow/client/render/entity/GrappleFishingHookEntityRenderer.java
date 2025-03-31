package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class GrappleFishingHookEntityRenderer extends EntityRenderer<GrappleFishingHookEntity> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/fishing_hook.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityCutout(TEXTURE);

    public GrappleFishingHookEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(GrappleFishingHookEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.push();
        matrices.scale(0.5F, 0.5F, 0.5F);
        matrices.multiply(this.dispatcher.getRotation());
        var entry = matrices.peek();
        var vertexConsumer = vertexConsumers.getBuffer(LAYER);
        vertex(vertexConsumer, entry, light, 0.0F, 0, 0, 1);
        vertex(vertexConsumer, entry, light, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, entry, light, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, entry, light, 0.0F, 1, 0, 0);
        matrices.pop();

        var owner = entity.getLivingOwner();
        if (owner != null) {
            var handAngle = MathHelper.sin(MathHelper.sqrt(owner.getHandSwingProgress(tickDelta)) * (float) Math.PI);
            var start = entity.getLerpedPos(tickDelta).add(0.0, 0.25, 0.0);
            var end = this.getHandPos(owner, handAngle, tickDelta);
            var difference = end.subtract(start);
            var normal = difference.normalize();

            var vertexConsumer2 = vertexConsumers.getBuffer(RenderLayer.getLines());
            var entry2 = matrices.peek();

            vertexConsumer2
                    .vertex(entry2, 0, 0, 0)
                    .color(Colors. BLACK)
                    .normal(entry2, (float) normal.x, (float) normal.y, (float) normal.z);
            vertexConsumer2
                    .vertex(entry2, (float) difference.x, (float) difference.y, (float) difference.z)
                    .color(Colors. BLACK)
                    .normal(entry2, (float) normal.x, (float) normal.y, (float) normal.z);
        }

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private Vec3d getHandPos(LivingEntity owner, float f, float tickDelta) {
        var offhand = !GrappleFishingHookEntity.hasFishingRodLoaded(owner.getMainHandStack());

        if (this.dispatcher.gameOptions.getPerspective().isFirstPerson() && owner == MinecraftClient.getInstance().player) {
            double m = 960.0 / (double) this.dispatcher.gameOptions.getFov().getValue();
            Vec3d vec3d = offhand
                    ? this.dispatcher.camera.getProjection().getPosition(-0.525F, 0f).multiply(m)
                    : this.dispatcher.camera.getProjection().getPosition(0, 0f).multiply(m).rotateX(f * 0.7F);
            return owner.getCameraPosVec(tickDelta).add(vec3d);
        } else {
            float g = MathHelper.lerp(tickDelta, owner.prevBodyYaw, owner.bodyYaw) * (float) (Math.PI / 180.0);
            double d = MathHelper.sin(g);
            double e = MathHelper.cos(g);
            float h = owner.getScale();
            double j = 0;
            double k = 0.8 * (double)h;
            float l = owner.isInSneakingPose() ? -0.1875F : 0.0F;
            return owner.getCameraPosVec(tickDelta).add(-e * j - d * k, (double)l - 0.45 * (double)h, -d * j + e * k);
        }
    }

    @Override
    public Identifier getTexture(GrappleFishingHookEntity entity) {
        return TEXTURE;
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry matrix, int light, float x, int y, int u, int v) {
        vertex(buffer, matrix, light, x - 0.5f, y - 0.5f, 0f, u, v);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry matrix, int light, Vec3d pos, int u, int v) {
        vertex(buffer, matrix, light, (float) pos.x, (float) pos.y, (float) pos.z, u, v);
    }

    private static void vertex(VertexConsumer buffer, MatrixStack.Entry matrix, int light, float x, float y, float z, int u, int v) {
        buffer.vertex(matrix, x, y, z)
                .color(Colors.WHITE)
                .texture((float)u, (float)v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(matrix, 0.0F, 1.0F, 0.0F);
    }
}
