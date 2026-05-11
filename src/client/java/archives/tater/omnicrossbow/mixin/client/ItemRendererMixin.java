package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.OmniEnchantment;
import archives.tater.omnicrossbow.client.render.OmniCrossbowRenderer;
import archives.tater.omnicrossbow.util.OmniUtil;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

@Mixin(value = ItemRenderer.class, priority = 900)
public abstract class ItemRendererMixin {
	@Shadow @Final private MinecraftClient client;

	@Inject(
			method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BakedModel;isBuiltin()Z")
	)
	private void renderAmmo(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (!(stack.getItem() instanceof CrossbowItem)) return;
		var projectile = OmniUtil.getMainProjectile(stack);
		if (OmniEnchantment.isNotDynamic(stack, projectile)) return;
		OmniCrossbowRenderer.renderProjectile((ItemRenderer) (Object) this, client.world, projectile, renderMode, leftHanded, matrices, vertexConsumers, light, overlay);
	}
}
