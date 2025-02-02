package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.client.render.OmniCrossbowRenderer;
import archives.tater.omnicrossbow.client.render.entity.BeaconLaserEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.EmberEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.EndCrystalProjectileEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.GenericItemProjectileEntityRenderer;
import archives.tater.omnicrossbow.client.render.gui.hud.MultichamberedIndicator;
import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.Identifier;

public class OmniCrossbowClient implements ClientModInitializer {
	private static final Identifier VIGNETTE_TEXTURE = new Identifier("textures/misc/vignette.png");

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(OmniCrossbowEntities.FREEZING_SNOWBALL, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.GENERIC_ITEM_PROJECTILE, GenericItemProjectileEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.BEACON_LASER, BeaconLaserEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.EMBER, EmberEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.SLIME_BALL, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.END_CRYSTAL_PROJECTILE, EndCrystalProjectileEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.SPY_ENDER_EYE, FlyingItemEntityRenderer::new);
		OmniCrossbowRenderer.register();
		BlockRenderLayerMap.INSTANCE.putBlock(OmniCrossbow.HONEY_SLICK_BLOCK, RenderLayer.getTranslucent());
		HudRenderCallback.EVENT.register(MultichamberedIndicator::drawIndicator);
		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			if (true) return;
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);
			RenderSystem.blendFuncSeparate(
					GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
			);
			RenderSystem.enableBlend();
			context.setShaderColor(0.12F, 0.68F, 0.43F, 1F);
			context.drawTexture(VIGNETTE_TEXTURE, 0, 0, -90, 0.0F, 0.0F, context.getScaledWindowWidth(), context.getScaledWindowHeight(), context.getScaledWindowWidth(), context.getScaledWindowHeight());
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();
			context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
		});
	}
}
