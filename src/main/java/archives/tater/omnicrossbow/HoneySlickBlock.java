package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.mixin.HoneyBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class HoneySlickBlock extends HoneyBlock {
    public static final EnumProperty<Direction> FACING = Properties.FACING;

    public static final Map<Direction, VoxelShape> FACE_SHAPES = Arrays.stream(Direction.values()).collect(Collectors.toUnmodifiableMap(
            direction -> direction,
            direction -> directionalLayer(direction, 4)));
    private static final Map<Direction, VoxelShape> SUPPORT_SHAPES = Arrays.stream(Direction.values()).collect(Collectors.toUnmodifiableMap(
            direction -> direction,
            direction -> directionalLayer(direction.getOpposite(), 1)));

    public HoneySlickBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.DOWN));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShape(state);
    }

    public VoxelShape getShape(BlockState state) {
        return FACE_SHAPES.get(state.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.getBoundingBox().intersects(getShape(state).offset(pos.getX(), pos.getY(), pos.getZ()).getBoundingBox())) { // This only works for simple voxelshapes
            var length = entity instanceof PlayerEntity ? entity.getPos().subtract(entity.prevX, entity.prevY, entity.prevZ).length() : entity.getVelocity().length();
            if (length >= 0.05)
                ((HoneyBlockInvoker) this).invokeAddCollisionEffects(world, entity);
            if (!entity.isOnGround() && entity.getVelocity().y < -0.08) {
                ((HoneyBlockInvoker) this).invokeUpdateSlidingVelocity(entity);
            } else {
                entity.slowMovement(state, new Vec3d(0.9F, 1.5, 0.9F));
            }
        }
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this) && state.get(FACING) == stateFrom.get(FACING);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var direction = state.get(FACING);
        var basePos = pos.offset(direction);
        return VoxelShapes.matchesAnywhere(world.getBlockState(basePos).getSidesShape(world, basePos).getFace(direction.getOpposite()), SUPPORT_SHAPES.get(direction), BooleanBiFunction.AND);
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        return direction == state.get(FACING) && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), Direction.DOWN, SideShapeType.FULL) && random.nextInt(16) == 0) {
            var box = state.getOutlineShape(world, pos).getBoundingBox();
            world.addParticle(ParticleTypes.DRIPPING_HONEY, box.getXLength() * random.nextDouble() + box.minX + pos.getX(), box.minY + pos.getY() - 0.05, box.getZLength() * random.nextDouble() + box.minZ + pos.getZ(), 0, 0, 0);
        }
    }

    private static VoxelShape directionalLayer(Direction direction, int thickness) {
        return createCuboidShape(
                direction == Direction.EAST ? 16 - thickness : 0,
                direction == Direction.UP ? 16 - thickness : 0,
                direction == Direction.SOUTH ? 16 - thickness : 0,
                direction == Direction.WEST ? thickness : 16,
                direction == Direction.DOWN ? thickness : 16,
                direction == Direction.NORTH ? thickness : 16
        );
    }

}
