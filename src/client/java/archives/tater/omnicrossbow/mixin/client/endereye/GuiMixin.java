package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private static Identifier VIGNETTE_LOCATION;

    @Inject(
            method = "extractCameraOverlays",
            at = @At("TAIL")
    )
    private void renderEyeOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        OmniCrossbowClient.renderEyeVignette(graphics, VIGNETTE_LOCATION);
    }
}
