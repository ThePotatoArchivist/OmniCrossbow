package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.duck.Grapplable;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin implements Grapplable {
    @Unique
    private @Nullable GrappleFishingHookEntity omnicrossbow$grappledHook = null;

    @Override
    public void omnicrossbow$setGrappledHook(@Nullable GrappleFishingHookEntity hook) {
        omnicrossbow$grappledHook = hook;
    }

    @Override
    public boolean omnicrossbow$isGrappled() {
        return omnicrossbow$grappledHook != null && !omnicrossbow$grappledHook.isRemoved() && !omnicrossbow$grappledHook.isPullingOwner();
    }

    @ModifyExpressionValue(
            method = "getFinalGravity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasNoGravity()Z")
    )
    private boolean checkGrappled(boolean original) {
        return original || omnicrossbow$isGrappled() || (this instanceof Grappler grappler && grappler.omnicrossbow$getHook() != null && grappler.omnicrossbow$getHook().isPullingOwner());
    }

    @WrapOperation(
            method = "getPosWithYOffset",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z", ordinal = 0)
    )
    private boolean checkWax(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original) {
        return original.call(instance, tagKey) || instance.isOf(OmniCrossbow.WAX_BLOCK);
    }
}
