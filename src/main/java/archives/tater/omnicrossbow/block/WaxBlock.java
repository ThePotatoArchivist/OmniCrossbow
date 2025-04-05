package archives.tater.omnicrossbow.block;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LichenGrower;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class WaxBlock extends MultifaceGrowthBlock {
    private final LichenGrower grower = new LichenGrower(this);

    public static final double MIN_BOOST_VELOCITY = 0.0625;
    public static final double MAX_BOOST_VELOCITY = 1.25;
    public static final BooleanProperty FRESH = BooleanProperty.of("fresh");

    public WaxBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FRESH, true));
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FRESH);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        world.scheduleBlockTick(pos, this, world.random.nextBetween(150, 300));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(FRESH))
            world.setBlockState(pos, state.with(FRESH, false));
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.handleFallDamage(fallDistance, 0.3f, entity.getDamageSources().fall());
        if (fallDistance < 1.5f) return;
        var horizontalVelocity = entity.getVelocity().horizontalLength();
        if (horizontalVelocity > MIN_BOOST_VELOCITY && horizontalVelocity < MAX_BOOST_VELOCITY) {
            entity.addVelocity(entity.getVelocity().multiply(1, 0, 1).normalize().multiply((MAX_BOOST_VELOCITY - horizontalVelocity)));
            entity.setSprinting(true);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (!state.get(FRESH)) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;
        livingEntity.addStatusEffect(new StatusEffectInstance(OmniCrossbow.WAXED_EFFECT, 60, 0));
        world.setBlockState(pos, state.with(FRESH, false));
    }

    public static boolean trySpread(World world, BlockPos origin, Direction side, boolean fresh, int range, int amount) {
        BlockState blockState;
        var currentState = world.getBlockState(origin);
        if (currentState.isOf(OmniCrossbow.WAX_BLOCK)) {
            blockState = currentState;
        } else {
            blockState = OmniCrossbow.WAX_BLOCK.withDirection(OmniCrossbow.WAX_BLOCK.getDefaultState().with(FRESH, fresh), world, origin, side);
            if (!currentState.isReplaceable() || blockState == null || !blockState.canPlaceAt(world, origin))
                return false;
            world.setBlockState(origin, blockState);
        }
        spread(world, origin, blockState, range, amount);

        return true;
    }

    private static void spread(World world, BlockPos pos, BlockState blockState, int range, int amount) {
        var grower = OmniCrossbow.WAX_BLOCK.getGrower();
        for (int i = 0; i < amount; i++) {
            grower.grow(blockState, world, pos, world.random).ifPresent(growPos -> {
                if (range > 0)
                    spread(world, growPos.pos(), world.getBlockState(growPos.pos()), range - 1, amount - 1);
            });
        }
    }
}
