package archives.tater.omnicrossbow.mixin.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEye;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public class ChunkMapTrackedEntityMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "removePlayer", at = @At("HEAD"), cancellable = true)
    public void removePlayerInject(ServerPlayer serverPlayer, CallbackInfo info) {
        if (!(this.entity instanceof SpyEnderEye spyEnderEye)) return;
        var owner = spyEnderEye.getOwner();
        if (owner == null || !owner.getUUID().equals(serverPlayer.getUUID())) return;
        info.cancel();
    }
}
