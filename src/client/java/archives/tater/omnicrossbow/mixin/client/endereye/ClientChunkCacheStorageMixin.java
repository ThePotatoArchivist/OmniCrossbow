package archives.tater.omnicrossbow.mixin.client.endereye;


import archives.tater.omnicrossbow.OmniCrossbowClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public abstract class ClientChunkCacheStorageMixin {
    @Inject(
            method = "inRange",
            at = @At("RETURN"),
            cancellable = true
    )
    private void chunkRange(int i, int j, CallbackInfoReturnable<Boolean> info) {
        if(OmniCrossbowClient.spyEyeUuid != null)
            info.setReturnValue(true);
    }
}
