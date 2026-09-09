package archives.tater.omnicrossbow.mixin.loot;

import archives.tater.omnicrossbow.registry.OmniCrossbowLoot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.server.packs.resources.Resource;

import com.google.gson.JsonElement;

@Debug(export = true)
@Mixin(ResourceManagerRegistryLoadTask.class)
public class ResourceManagerRegistryLoadTaskMixin {

    /// @see net.fabricmc.fabric.mixin.loot.ResourceManagerRegistryLoadTaskMixin
    @WrapOperation(
            method = "lambda$load$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryLoadTask$PendingRegistration;loadFromResource(Lcom/mojang/serialization/Decoder;Lnet/minecraft/resources/RegistryOps;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/server/packs/resources/Resource;)Lcom/mojang/datafixers/util/Either;"),
            order = 1100
    )
    private <T> Either<T, Exception> saveRegistryLookup(Decoder<T> elementDecoder, RegistryOps<JsonElement> ops, ResourceKey<T> elementKey, Resource thunk, Operation<Either<T, Exception>> original) {
        return ScopedValue.where(OmniCrossbowLoot.REGISTRY_OPS, ops).call(() -> original.call(elementDecoder, ops, elementKey, thunk));
    }
}
