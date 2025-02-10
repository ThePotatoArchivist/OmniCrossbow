package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow public abstract void enterCombat();

    @WrapWithCondition(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V")
    )
    private boolean checkEnderEye(ServerPlayerEntity instance, double x, double y, double z, float yaw, float pitch, @Local Entity cameraEntity) {
        return !(cameraEntity instanceof SpyEnderEyeEntity);
    }

    @Inject(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getWorld()Lnet/minecraft/world/World;")
    )
    private void storeIsEnderEye(Entity entity, CallbackInfo ci, @Local(ordinal = 1) Entity prevCameraEntity, @Share("isEnderEye") LocalBooleanRef isEnderEye) {
        isEnderEye.set(entity instanceof SpyEnderEyeEntity || prevCameraEntity instanceof SpyEnderEyeEntity);
    }

    @WrapWithCondition(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z")
    )
    private boolean checkIsEnderEye(ServerPlayerEntity instance, ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, @Share("isEnderEye") LocalBooleanRef isEnderEye) {
        return !isEnderEye.get();
    }

    @WrapWithCondition(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;updatePosition(Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    )
    private boolean checkIsEnderEye(ServerChunkManager instance, ServerPlayerEntity player, @Share("isEnderEye") LocalBooleanRef isEnderEye) {
        return !isEnderEye.get();
    }

    @WrapWithCondition(
            method = "setCameraEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;syncWithPlayerPosition()V")
    )
    private boolean checkIsEnderEye(ServerPlayNetworkHandler instance, @Share("isEnderEye") LocalBooleanRef isEnderEye) {
        return !isEnderEye.get();
    }

    @ModifyExpressionValue(
            method = "onExplodedBy",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;")
    )
    private EntityType<?> checkLargeWindCharge(EntityType<?> original, @Local(argsOnly = true) @Nullable Entity entity) {
        return original == OmniCrossbowEntities.LARGE_WIND_CHARGE ? EntityType.WIND_CHARGE : original;
    }
}
