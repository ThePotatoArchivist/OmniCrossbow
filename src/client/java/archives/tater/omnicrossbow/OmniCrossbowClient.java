package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.client.render.OmniCrossbowRenderer;
import archives.tater.omnicrossbow.client.render.entity.*;
import archives.tater.omnicrossbow.client.render.gui.hud.MultichamberedIndicator;
import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.TntEntityRenderer;
import net.minecraft.client.render.entity.WindChargeEntityRenderer;
import net.minecraft.util.Identifier;

public class OmniCrossbowClient implements ClientModInitializer {
	private static final Identifier VIGNETTE_TEXTURE = Identifier.ofVanilla("textures/misc/vignette.png");

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
		EntityRendererRegistry.register(OmniCrossbowEntities.LARGE_WIND_CHARGE, WindChargeEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, GrappleFishingHookEntityRenderer::new);
		if (OmniCrossbowEntities.AREA_TNT != null)
			EntityRendererRegistry.register(OmniCrossbowEntities.AREA_TNT, TntEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlock(OmniCrossbow.HONEY_SLICK_BLOCK, RenderLayer.getTranslucent());
		OmniCrossbowRenderer.register();
		MultichamberedIndicator.register();
        OmniCrossbow.CLIENT_NETWORKING = ClientPlayNetworking::send;
	}
}
