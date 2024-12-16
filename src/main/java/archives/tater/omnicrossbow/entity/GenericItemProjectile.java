package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.LivingEntityAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GenericItemProjectile extends ThrownItemEntity {
    public GenericItemProjectile(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public GenericItemProjectile(double d, double e, double f, World world) {
        super(OmniCrossbowEntities.GENERIC_ITEM_PROJECTILE, d, e, f, world);
    }

    public GenericItemProjectile(LivingEntity livingEntity, World world) {
        super(OmniCrossbowEntities.GENERIC_ITEM_PROJECTILE, livingEntity, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COD;
    }

    // Call server side
    private ItemEntity dropAt(HitResult hitResult) {
        ItemEntity itemEntity = new ItemEntity(this.getWorld(), hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z, getStack());
        itemEntity.setToDefaultPickupDelay();
        itemEntity.setVelocity(0, 0, 0);
        this.getWorld().spawnEntity(itemEntity);
        return itemEntity;
    }

    private FakePlayer createFakePlayer() {
        @Nullable var owner = getOwner();
        var fakePlayer = FakePlayer.get((ServerWorld) getWorld(), owner == null ? new GameProfile(FakePlayer.DEFAULT_UUID, "a crossbow projectile") : new GameProfile(owner.getUuid(), "a crossbow projectile shot by " + owner.getName()));
        fakePlayer.refreshPositionAndAngles(getX(), getY(), getZ(), -getYaw(), getPitch()); // idk why yaw is negative but it's negative
        fakePlayer.setStackInHand(Hand.MAIN_HAND, getStack());
        ((LivingEntityAccessor) fakePlayer).invokeGetEquipmentChanges();
        ((LivingEntityAccessor) fakePlayer).setLastAttackedTicks(MathHelper.ceil(fakePlayer.getAttackCooldownProgressPerTick()));
        return fakePlayer;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (getWorld().isClient) return;
        var stack = getStack();
        customBlockActions(blockHitResult, stack);
        if (!stack.isEmpty()) dropAt(blockHitResult);
    }

    private boolean customBlockActions(BlockHitResult blockHitResult, ItemStack stack) {
        var fakePlayer = createFakePlayer();
        var blockPos = blockHitResult.getBlockPos();
        var state = getWorld().getBlockState(blockPos);

        if (isSuitableTool(stack, blockPos, state, fakePlayer) && ((ServerWorld) getWorld()).getServer().getPlayerInteractionManager(fakePlayer).tryBreakBlock(blockPos)) return true;
        if (stack.useOnBlock(new ItemUsageContext(getWorld(), fakePlayer, Hand.MAIN_HAND, stack, blockHitResult)).isAccepted()) return true;
        if (stack.useOnBlock(new ItemUsageContext(getWorld(), fakePlayer, Hand.MAIN_HAND, stack, new BlockHitResult(blockHitResult.getPos(), blockHitResult.getSide(), blockPos.offset(blockHitResult.getSide()), true))).isAccepted()) return true;

        return false;
    }

    private boolean isSuitableTool(ItemStack tool, BlockPos blockPos, BlockState state, PlayerEntity fakePlayer) {
        return tool.isSuitableFor(state) || tool.isIn(ConventionalItemTags.SHEARS) && state.isIn(BlockTags.LEAVES) || (tool.getItem() instanceof MiningToolItem && !state.isIn(BlockTags.AXE_MINEABLE) && !state.isIn(BlockTags.HOE_MINEABLE) && !state.isIn(BlockTags.PICKAXE_MINEABLE) && !state.isIn(BlockTags.PICKAXE_MINEABLE) & state.calcBlockBreakingDelta(fakePlayer, getWorld(), blockPos) >= 0.005);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (getWorld().isClient) return;
        var stack = getStack();
        customEntityActions(entityHitResult, stack);
        if (!stack.isEmpty()) dropAt(entityHitResult);
    }

    private void customEntityActions(EntityHitResult entityHitResult, ItemStack itemStack) {
        var entity = entityHitResult.getEntity();
        var fakePlayer = createFakePlayer();

        if (entity.interact(fakePlayer, Hand.MAIN_HAND).isAccepted()) return;
        if (entity instanceof LivingEntity livingEntity && getOwner() instanceof PlayerEntity ownerPlayer)
            if (itemStack.useOnEntity(ownerPlayer, livingEntity, Hand.MAIN_HAND).isAccepted()) return;

        // Still use the original player for damaging so that mobs don't aggro on a ghost player
        var damage = (float) fakePlayer.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + EnchantmentHelper.getAttackDamage(itemStack, entity instanceof LivingEntity livingEntity ? livingEntity.getGroup() : EntityGroup.DEFAULT);
        entity.damage(getWorld().getDamageSources().thrown(this, getOwner()), damage);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient) discard();
    }
}
