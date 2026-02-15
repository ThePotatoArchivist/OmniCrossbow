package archives.tater.omnicrossbow.mixin.client.enchantmenteffect.spin;

import archives.tater.omnicrossbow.client.render.CrossbowSpinRendering;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin {
    @Inject(
            method = "extractArmedEntityRenderState",
            at = @At("TAIL")
    )
    private static void saveSpin(LivingEntity entity, ArmedEntityRenderState state, ItemModelResolver itemModelResolver, float partialTicks, CallbackInfo ci) {
        state.setData(CrossbowSpinRendering.SPINNING_ITEM, entity.hasAttached(OmniCrossbowAttachments.SPINNING_ITEM));
    }
}
