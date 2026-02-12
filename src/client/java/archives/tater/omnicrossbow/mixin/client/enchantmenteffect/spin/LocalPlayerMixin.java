package archives.tater.omnicrossbow.mixin.client.enchantmenteffect.spin;

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;
import archives.tater.omnicrossbow.registry.OmniCrossbowComponents;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

@SuppressWarnings("UnstableApiUsage")
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    public LocalPlayerMixin(ClientLevel level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @ModifyExpressionValue(
            method = {
                    "isSlowDueToUsingItem",
                    "itemUseSpeedMultiplier"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object spinOverrideMovement(Object original) {
        return isUsingItem() && hasAttached(OmniCrossbowAttachments.SPINNING_ITEM) ? OmniCrossbowComponents.SPIN_CROSSBOW_USAGE : original;
    }
}
