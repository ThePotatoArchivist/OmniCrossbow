package archives.tater.omnicrossbow.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.LichenGrower;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WaxBlock extends MultifaceGrowthBlock {
    private final LichenGrower grower = new LichenGrower(this);

    public static final double MIN_BOOST_VELOCITY = 0.0625;
    public static final double MAX_BOOST_VELOCITY = 1.25;

    public WaxBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends MultifaceGrowthBlock> getCodec() {
        return null;
    }

    @Override
    public LichenGrower getGrower() {
        return grower;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        super.onEntityLand(world, entity);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        var horizontalVelocity = entity.getVelocity().horizontalLength();
        if (horizontalVelocity > MIN_BOOST_VELOCITY && horizontalVelocity < MAX_BOOST_VELOCITY)
            entity.addVelocity(entity.getVelocity().multiply(1, 0, 1).normalize().multiply((MAX_BOOST_VELOCITY - horizontalVelocity)));
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
    }
}
