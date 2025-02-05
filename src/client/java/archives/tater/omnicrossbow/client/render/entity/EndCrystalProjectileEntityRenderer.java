package archives.tater.omnicrossbow.client.render.entity;

import archives.tater.omnicrossbow.entity.EndCrystalProjectileEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class EndCrystalProjectileEntityRenderer extends EntityRenderer<EndCrystalProjectileEntity> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal.png");
    private static final RenderLayer END_CRYSTAL = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    private static final float SINE_45_DEGREES = (float)Math.sin(Math.PI / 4);
    private static final String GLASS = "glass";
    private final ModelPart core;
    private final ModelPart frame;

    public EndCrystalProjectileEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        ModelPart modelPart = context.getPart(EntityModelLayers.END_CRYSTAL);
        this.frame = modelPart.getChild(GLASS);
        this.core = modelPart.getChild(EntityModelPartNames.CUBE);
    }

    @Override
    protected int getBlockLight(EndCrystalProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLight(EndCrystalProjectileEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(EndCrystalProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        float age = (entity.age + tickDelta) * 3.0F;
        var vertexConsumer = vertexConsumers.getBuffer(END_CRYSTAL);
        matrices.scale(2.0F, 2.0F, 2.0F);
        matrices.translate(0.0F, -0.5F, 0.0F);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age));
        matrices.translate(0.0F, 1.5F / 2.0F, 0.0F);
        matrices.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        this.frame.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.scale(0.875F, 0.875F, 0.875F);
        matrices.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age));
        this.frame.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.scale(0.875F, 0.875F, 0.875F);
        matrices.multiply(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age));
        this.core.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(EndCrystalProjectileEntity entity) {
        return TEXTURE;
    }
}
