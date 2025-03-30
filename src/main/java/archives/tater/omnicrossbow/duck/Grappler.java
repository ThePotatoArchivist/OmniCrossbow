package archives.tater.omnicrossbow.duck;

import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import org.jetbrains.annotations.Nullable;

public interface Grappler {
    @Nullable GrappleFishingHookEntity omnicrossbow$getHook();
    void omnicrossbow$setHook(@Nullable GrappleFishingHookEntity hook);
}
