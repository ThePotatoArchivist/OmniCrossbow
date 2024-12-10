package archives.tater.omnicrossbow.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
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

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (getWorld().isClient) return;
        var stack = getStack();
        customBlockActions(blockHitResult, stack);
        if (!stack.isEmpty()) dropAt(blockHitResult);
    }

    private boolean customBlockActions(BlockHitResult blockHitResult, ItemStack stack) {
        @Nullable PlayerEntity ownerPlayer = getOwner() instanceof PlayerEntity playerEntity ? playerEntity : null;
        var blockPos = blockHitResult.getBlockPos();

        if (stack.getItem() instanceof MiningToolItem miningToolItem && miningToolItem.isSuitableFor(getWorld().getBlockState(blockPos))) { // TODO: more checks
            var state = getWorld().getBlockState(blockPos);
            getWorld().breakBlock(blockPos, true, getOwner());
            try {
                miningToolItem.postMine(stack, getWorld(), state, blockPos, ownerPlayer);
            } catch (NullPointerException ignored) {}
            return true;
        }
        try {
            if (stack.useOnBlock(new ItemUsageContext(getWorld(), ownerPlayer, Hand.MAIN_HAND, stack, blockHitResult)).isAccepted()) return true;
        } catch (NullPointerException ignored) {}
        try {
            if (stack.useOnBlock(new ItemUsageContext(getWorld(), ownerPlayer, Hand.MAIN_HAND, stack, new BlockHitResult(blockHitResult.getPos(), blockHitResult.getSide(), blockPos.offset(blockHitResult.getSide()), true))).isAccepted()) return true;
        } catch (NullPointerException ignored) {}
        return false;
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
        if (entity instanceof SheepEntity sheepEntity && itemStack.getItem() instanceof ShearsItem) {
            sheepEntity.sheared(getOwner() == null ? SoundCategory.PLAYERS : getOwner().getSoundCategory());
            sheepEntity.emitGameEvent(GameEvent.SHEAR, getOwner());
            itemStack.damage(1, getWorld().random, null);
            return;
        }
        if (entity instanceof LivingEntity livingEntity && getOwner() instanceof PlayerEntity ownerPlayer)
            if (itemStack.useOnEntity(ownerPlayer, livingEntity, Hand.MAIN_HAND).isAccepted()) return;
        entity.damage(getWorld().getDamageSources().thrown(this, getOwner()), 1f); // TODO: different damage values
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient) discard();
    }
}
