package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.client.render.OmniCrossbowRenderer;
import archives.tater.omnicrossbow.entity.GenericItemProjectile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class GenericItemProjectileEntityRenderer extends EntityRenderer<GenericItemProjectile> {
    private final ItemRenderer itemRenderer;

    public GenericItemProjectileEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GenericItemProjectile entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        var stack = entity.getStack();
        var nonBillboard = OmniCrossbowRenderer.projectileNonBillboard(stack.getItem());
        if (nonBillboard) {
            matrices.translate(0, entity.getHeight() / 2, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-135));
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.translate(0.25f, -0.25f, 0);
            OmniCrossbowRenderer.itemSpecificTransform(stack.getItem(), matrices);
            matrices.scale(-1, 1, -1); // rotate 180 on y
        } else {
            matrices.multiply(this.dispatcher.getRotation());
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        }
        itemRenderer.renderItem(stack, nonBillboard ? ModelTransformationMode.FIXED : ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Identifier getTexture(GenericItemProjectile entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
