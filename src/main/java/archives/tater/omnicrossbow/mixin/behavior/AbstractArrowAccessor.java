package archives.tater.omnicrossbow.mixin.behavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Accessor
    void setFiredFromWeapon(ItemStack weaponItem);

    @Invoker
    void invokeSetPickupItemStack(ItemStack stack);
}
