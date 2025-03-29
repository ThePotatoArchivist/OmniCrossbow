package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements Grappler {
    @Unique
    private @Nullable GrappleFishingHookEntity omnicrossbow$hook = null;

    @Override
    public GrappleFishingHookEntity omnicrossbow$getHook() {
        return omnicrossbow$hook;
    }

    @Override
    public void omnicrossbow$setHook(GrappleFishingHookEntity hook) {
        omnicrossbow$hook = hook;
    }
}
