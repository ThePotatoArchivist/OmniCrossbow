package archives.tater.omnicrossbow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrialSpawnerLogic.class)
public class TrialSpawnerLogicMixin {
    @WrapOperation(
            method = "canActivate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z")
    )
    private boolean enableTrialSpawner(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule, Operation<Boolean> original) {
        return true;
    }
}
