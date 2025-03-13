package archives.tater.omnicrossbow.mixin.arealib;

import archives.tater.omnicrossbow.area.OmniCrossbowAreaLibCompat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "isBlockBreakingRestricted",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;canBreak(Lnet/minecraft/block/pattern/CachedBlockPosition;)Z")
    )
    private boolean checkArea(boolean original, @Local(argsOnly = true) World world, @Local(argsOnly = true) BlockPos pos) {
        return original || OmniCrossbowAreaLibCompat.containedInModifiableArea(world, pos);
    }

    @ModifyExpressionValue(
            method = "canPlaceOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;canPlaceOn(Lnet/minecraft/block/pattern/CachedBlockPosition;)Z")
    )
    private boolean allowInArea(boolean original, @Local(argsOnly = true) BlockPos pos) {
        return original || OmniCrossbowAreaLibCompat.containedInModifiableArea(getWorld(), pos);
    }
}
