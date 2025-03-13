package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.area.AreaCheckExplosionBehavior;
import archives.tater.omnicrossbow.duck.ReplacesExplosionBehavior;
import archives.tater.omnicrossbow.mixin.TntEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class AreaTntEntity extends TntEntity implements ReplacesExplosionBehavior {
    public AreaTntEntity(EntityType<? extends TntEntity> entityType, World world) {
        super(entityType, world);
    }

    public AreaTntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        this(OmniCrossbowEntities.AREA_TNT, world);
        this.setPosition(x, y, z);
        double d = world.random.nextDouble() * (double)((float)Math.PI * 2F);
        this.setVelocity(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        ((TntEntityAccessor) this).setCausingEntity(igniter);
    }

    @Override
    public @Nullable ExplosionBehavior omni$getExplosionBehavior(@Nullable ExplosionBehavior original) {
        return new AreaCheckExplosionBehavior(original);
    }
}
