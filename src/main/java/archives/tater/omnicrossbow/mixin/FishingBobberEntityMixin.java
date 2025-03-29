package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
    public FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void superTickOnly(CallbackInfo ci) {
        // Yes this is a hack I know
        if (!((Object) this instanceof GrappleFishingHookEntity))
            return;
        super.tick();
        ci.cancel();
    }
}
