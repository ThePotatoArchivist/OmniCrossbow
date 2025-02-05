package archives.tater.omnicrossbow.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemInvoker {
    @Invoker
    static float invokeGetSoundPitch(Random random, int index) {
        throw new AssertionError();
    }

    @Invoker
    void invokeShoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target);

    @Invoker
    static Vector3f invokeCalcVelocity(LivingEntity shooter, Vec3d direction, float yaw) {
        throw new AssertionError();
    }
}
