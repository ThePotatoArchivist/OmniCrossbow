package archives.tater.lockedloaded.mixin.client.enchantmenteffect.spin;

import archives.tater.lockedloaded.client.render.CrossbowSpinRendering;
import archives.tater.lockedloaded.registry.LockedLoadedAttachments;

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
        state.setData(CrossbowSpinRendering.SPINNING_ITEM, entity.hasAttached(LockedLoadedAttachments.SPINNING_ITEM));
    }
}
