package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.client.render.entity.BeaconLaserEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.EmberEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.GenericItemProjectileEntityRenderer;
import archives.tater.omnicrossbow.client.render.entity.OmniCrossbowRenderer;
import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class OmniCrossbowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(OmniCrossbowEntities.FREEZING_SNOWBALL, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.GENERIC_ITEM_PROJECTILE, GenericItemProjectileEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.BEACON_LASER, BeaconLaserEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.EMBER, EmberEntityRenderer::new);
		OmniCrossbowRenderer.register();
	}
}
