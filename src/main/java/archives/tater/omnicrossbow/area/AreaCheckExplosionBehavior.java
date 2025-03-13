package archives.tater.omnicrossbow.area;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AreaCheckExplosionBehavior extends ExplosionBehavior {
    private final @Nullable ExplosionBehavior delegate;

    public AreaCheckExplosionBehavior(@Nullable ExplosionBehavior delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
        return (delegate == null ? super.canDestroyBlock(explosion, world, pos, state, power) : delegate.canDestroyBlock(explosion, world, pos, state, power)) && OmniCrossbowAreaLibCompat.containedInModifiableArea(world instanceof World world1 ? world1 : null, pos);
    }

    @Override
    public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        return delegate == null ? super.getBlastResistance(explosion, world, pos, blockState, fluidState) : delegate.getBlastResistance(explosion, world, pos, blockState, fluidState);
    }

    @Override
    public boolean shouldDamage(Explosion explosion, Entity entity) {
        return delegate == null ? super.shouldDamage(explosion, entity) : delegate.shouldDamage(explosion, entity);
    }

    @Override
    public float getKnockbackModifier(Entity entity) {
        return delegate == null ? super.getKnockbackModifier(entity) : delegate.getKnockbackModifier(entity);
    }

    @Override
    public float calculateDamage(Explosion explosion, Entity entity) {
        return delegate == null ? super.calculateDamage(explosion, entity) : delegate.calculateDamage(explosion, entity);
    }
}
