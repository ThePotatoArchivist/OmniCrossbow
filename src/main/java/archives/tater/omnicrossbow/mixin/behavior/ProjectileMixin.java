package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {
    public ProjectileMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyVariable(
            method = "shoot",
            at = @At("HEAD"),
            argsOnly = true,
            name = "pow"
    )
    private float scaleVelocity(float velocity) {
        return velocity * getAttachedOrElse(OmniCrossbowAttachments.PROJECTILE_BEHAVIOR, ProjectileBehavior.DEFAULT).velocityScale();
    }
}
