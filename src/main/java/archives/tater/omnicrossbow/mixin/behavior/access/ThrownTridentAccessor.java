package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;

@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
    @Accessor
    static EntityDataAccessor<Byte> getID_LOYALTY() {
        throw new AssertionError();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getID_FOIL() {
        throw new AssertionError();
    }

    @Invoker
    byte invokeGetLoyaltyFromItem(ItemStack tridentItem);
}
