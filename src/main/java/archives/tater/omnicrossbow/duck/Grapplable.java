package archives.tater.omnicrossbow.duck;

import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import org.jetbrains.annotations.Nullable;

public interface Grapplable {
    void omnicrossbow$setGrappledHook(@Nullable GrappleFishingHookEntity hook);

    boolean omnicrossbow$isGrappled();
}
