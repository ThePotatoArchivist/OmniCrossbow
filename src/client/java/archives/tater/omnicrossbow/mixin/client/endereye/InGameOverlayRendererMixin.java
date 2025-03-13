package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @ModifyExpressionValue(
            method = "renderOverlays",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z")
    )
    private static boolean checkEnderEye(boolean original, @Local(argsOnly = true) MinecraftClient client) {
        return original && !(client.getCameraEntity() instanceof SpyEnderEyeEntity);
    }

    @WrapOperation(
            method = "renderOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameOverlayRenderer;getInWallBlockState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;")
    )
    private static BlockState useEnderEyeBlockState(PlayerEntity player, Operation<BlockState> original, @Local(argsOnly = true) MinecraftClient client) {
        if (!(client.getCameraEntity() instanceof SpyEnderEyeEntity)) return original.call(player);
        var pos = client.getCameraEntity().getBlockPos();
        var blockState = player.getWorld().getBlockState(pos);
        return blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(player.getWorld(), pos)
                ? blockState : null;
    }
}
