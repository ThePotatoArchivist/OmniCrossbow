package archives.tater.omnicrossbow.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

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

    private void customBlockActions(BlockHitResult blockHitResult, ItemStack stack) {
        if (stack.getItem() instanceof ToolItem) { // TODO: more checks
            getWorld().breakBlock(blockHitResult.getBlockPos(), true, getOwner());
            return;
        }
        // TODO: catch NullPointerExceptions
        if (stack.useOnBlock(new ItemUsageContext(getWorld(), null, Hand.MAIN_HAND, stack, blockHitResult)).isAccepted()) return;
        if (stack.useOnBlock(new ItemUsageContext(getWorld(), null, Hand.MAIN_HAND, stack, new BlockHitResult(blockHitResult.getPos(), blockHitResult.getSide(), blockHitResult.getBlockPos().offset(blockHitResult.getSide()), true))).isAccepted()) return;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (getWorld().isClient) return;
        entityHitResult.getEntity().damage(getWorld().getDamageSources().thrown(this, getOwner()), 1f); // TODO: different damage values
        dropAt(entityHitResult);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient) discard();
    }
}
