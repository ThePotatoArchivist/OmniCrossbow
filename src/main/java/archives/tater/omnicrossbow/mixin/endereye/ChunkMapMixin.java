package archives.tater.omnicrossbow.mixin.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEye;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "markChunkPendingToSend(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At("HEAD"), cancellable = true)
    private static void markChunkPendingToSendInject(ServerPlayer serverPlayer, LevelChunk levelChunk, CallbackInfo info) {
        if (!(serverPlayer instanceof SpyEnderEye.EyeFakePlayer fakePlayer)) return;
        var owner = fakePlayer.getEyeEntity().getOwner();
        if (owner == null) return;

        owner.connection.chunkSender.markChunkPendingToSend(levelChunk);
        info.cancel();
    }

    @Inject(method = "dropChunk(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V", at = @At("HEAD"), cancellable = true)
    private static void dropChunkInject(ServerPlayer serverPlayer, ChunkPos chunkPos, CallbackInfo info) {
        if (!(serverPlayer instanceof SpyEnderEye.EyeFakePlayer fakePlayer)) return;
        var owner = fakePlayer.getEyeEntity().getOwner();
        if (owner == null) return;

        owner.connection.chunkSender.dropChunk(owner, chunkPos);
        info.cancel();
    }
}
