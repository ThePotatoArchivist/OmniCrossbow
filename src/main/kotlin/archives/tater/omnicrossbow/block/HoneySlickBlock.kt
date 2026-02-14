package archives.tater.omnicrossbow.block

import archives.tater.omnicrossbow.mixin.HoneyBlockInvoker
import archives.tater.omnicrossbow.util.get
import archives.tater.omnicrossbow.util.isOf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.InsideBlockEffectApplier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape


class HoneySlickBlock(properties: Properties) : HoneyBlock(properties) {
    init {
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = FACE_SHAPES[state[FACING]]!!

    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = Shapes.empty()

    override fun entityInside(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        entity: Entity,
        effectApplier: InsideBlockEffectApplier,
        isPrecise: Boolean
    ) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        this as HoneyBlockInvoker

        if (!entity.boundingBox.intersects(state.getShape(level, pos).move(pos).bounds())) return

        val length = entity.deltaMovement.length()

        if (!entity.onGround() && length >= 0.05)
            invokeMaybeDoSlideEffects(level, entity)

        if (!entity.onGround() && entity.deltaMovement.y < -0.08)
            invokeDoSlideMovement(entity)
        else
            entity.makeStuckInBlock(state, Vec3(0.9, 1.5, 0.9))
    }

    override fun skipRendering(state: BlockState, neighborState: BlockState, direction: Direction): Boolean =
        super.skipRendering(state, neighborState, direction) && state[FACING] == neighborState[FACING]

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, context.clickedFace.opposite)

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val direction = state[FACING]
        val basePos = pos.relative(direction.opposite)
        return Shapes.joinIsNotEmpty(level[basePos].getBlockSupportShape(level, basePos).getFaceShape(direction.opposite), SUPPORT_SHAPES[direction]!!, BooleanOp.AND)
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource
    ): BlockState = if (directionToNeighbour == state[FACING] && !state.canSurvive(level, pos))
        Blocks.AIR.defaultBlockState()
    else
        super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random)

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (!(itemStack isOf Items.GLASS_BOTTLE)) return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult)
        level.destroyBlock(pos, false, player)
        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemUtils.createFilledResult(itemStack, player, Items.HONEY_BOTTLE.defaultInstance))
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (level[pos.below()].isFaceSturdy(level, pos, Direction.DOWN, SupportType.FULL) || random.nextInt(10) != 0) return

        val aabb = state.getShape(level, pos).bounds()

        level.addParticle(
            ParticleTypes.DRIPPING_HONEY,
            aabb.xsize * random.nextDouble() + aabb.minX + pos.x,
            aabb.minY + pos.y - 0.05,
            aabb.zsize * random.nextDouble() + aabb.minZ + pos.z,
            0.0, 0.0, 0.0
        )
    }

    companion object {
        val FACING: EnumProperty<Direction> = DirectionalBlock.FACING

        private fun directionalLayer(direction: Direction, thickness: Double): VoxelShape = box(
            if (direction == Direction.WEST) 16.0 - thickness else 0.0,
            if (direction == Direction.DOWN) 16.0 - thickness else 0.0,
            if (direction == Direction.NORTH) 16.0 - thickness else 0.0,
            if (direction == Direction.EAST) thickness else 16.0,
            if (direction == Direction.UP) thickness else 16.0,
            if (direction == Direction.SOUTH) thickness else 16.0
        )

        val FACE_SHAPES = Direction.entries.associateWith {
            directionalLayer(it, 4.0)
        }

        val SUPPORT_SHAPES = Direction.entries.associateWith {
            directionalLayer(it.opposite, 1.0)
        }
    }
}