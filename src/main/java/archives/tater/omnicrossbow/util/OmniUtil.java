package archives.tater.omnicrossbow.util;

import archives.tater.omnicrossbow.mixin.CrossbowItemInvoker;
import net.minecraft.item.ItemStack;

public class OmniUtil {
    public static ItemStack getMainProjectile(ItemStack crossbow) {
        var projectiles = CrossbowItemInvoker.invokeGetProjectiles(crossbow);
        return projectiles.isEmpty() ? ItemStack.EMPTY : projectiles.get(0);
    }
}
