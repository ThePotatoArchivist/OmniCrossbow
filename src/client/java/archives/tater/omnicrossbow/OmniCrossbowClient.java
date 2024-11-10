package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class OmniCrossbowClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(OmniCrossbowEntities.CROSSBOW_SNOWBALL, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(OmniCrossbowEntities.DELAYED_SONIC_BOOM, EmptyEntityRenderer::new);
	}
}
