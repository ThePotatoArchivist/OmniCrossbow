package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Player.class)
public interface PlayerInvoker {
    @Invoker
    void invokeItemAttackInteraction(final Entity entity, final ItemStack attackingItemStack, final DamageSource damageSource, final boolean applyToTarget);
}
