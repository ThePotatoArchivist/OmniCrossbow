package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.mixin.BoatItemAccessor;
import archives.tater.omnicrossbow.mixin.EntityBucketItemAccessor;
import archives.tater.omnicrossbow.mixin.FallingBlockInvoker;
import net.minecraft.block.FallingBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static archives.tater.omnicrossbow.mixin.FallingBlockEntityInvoker.newFallingBlockEntity;

public class OmniEnchantment extends Enchantment {
    protected OmniEnchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, EnchantmentTarget.CROSSBOW, slotTypes);
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return super.canAccept(other) && other != Enchantments.MULTISHOT && other != Enchantments.PIERCING;
    }

    private static Vec3d getProjectileVel(LivingEntity shooter, double length) {
        return shooter.getRotationVector().multiply(length);
    }

    @FunctionalInterface
    interface ExplosiveConstructor {
        ExplosiveProjectileEntity create(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ);
    }

    private static ExplosiveProjectileEntity shootExplosive(World world, LivingEntity shooter, double length, ExplosiveConstructor constructor) {
        var velocity = getProjectileVel(shooter, length);
        return constructor.create(world, shooter, velocity.x, velocity.y, velocity.z);
    }

    private static @Nullable Entity create(ServerWorld world, EntityType<?> type, LivingEntity shooter, ItemStack projectile, SpawnReason spawnReason) {
        var entity = type.create(world, projectile.getNbt(), null, shooter.getBlockPos(), spawnReason, false, false);
        if (entity == null) return null;
        entity.refreshPositionAndAngles(shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), shooter.getYaw(), entity.getPitch());
        return entity;
    }

    public static @Nullable Entity createProjectile(ServerWorld world, LivingEntity shooter, ItemStack projectile) {
        if (projectile.isOf(Items.EGG)) return new EggEntity(world, shooter);
        if (projectile.isOf(Items.ENDER_PEARL)) return new EnderPearlEntity(world, shooter);
        if (projectile.isOf(Items.SNOWBALL)) return new SnowballEntity(world, shooter);
        if (projectile.isOf(Items.EXPERIENCE_BOTTLE)) return new ExperienceBottleEntity(world, shooter);
        if (projectile.isOf(Items.TRIDENT)) {
            projectile.damage(1, shooter, (livingEntity) -> {});
            var entity = new TridentEntity(world, shooter, projectile);
            if (shooter instanceof PlayerEntity playerEntity && playerEntity.getAbilities().creativeMode) {
                entity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
            return entity;
        }
        if (projectile.isOf(Items.ENDER_EYE)) return new EyeOfEnderEntity(world, shooter.getX(), shooter.getBodyY(0.5), shooter.getZ());
        if (projectile.isOf(Items.TNT)) return new TntEntity(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), shooter);
        if (projectile.isOf(Items.WITHER_SKELETON_SKULL)) return shootExplosive(world, shooter, 1f, WitherSkullEntity::new);
        if (projectile.isOf(Items.FIRE_CHARGE)) return shootExplosive(world, shooter, 1f, SmallFireballEntity::new);
        if (projectile.isOf(Items.DRAGON_BREATH)) return shootExplosive(world, shooter, 1f, DragonFireballEntity::new);
        if (projectile.isOf(Items.ARMOR_STAND)) return create(world, EntityType.ARMOR_STAND, shooter, projectile, SpawnReason.SPAWN_EGG);

        var projectileItem = projectile.getItem();
        if (projectileItem instanceof ThrowablePotionItem) return new PotionEntity(world, shooter);
        if (projectileItem instanceof BlockItem blockItem && blockItem.getBlock() instanceof FallingBlock fallingBlock) {
            var entity =  newFallingBlockEntity(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), fallingBlock.getDefaultState());
            ((FallingBlockInvoker) fallingBlock).invokeConfigureFallingBlockEntity(entity);
            return entity;
        }
        if (projectileItem instanceof EntityBucketItem entityBucketItem) return create(world, ((EntityBucketItemAccessor) entityBucketItem).getEntityType(), shooter, projectile, SpawnReason.BUCKET);
        if (projectileItem instanceof SpawnEggItem spawnEggItem) return create(world, spawnEggItem.getEntityType(null), shooter, projectile, SpawnReason.SPAWN_EGG);
        if (projectileItem instanceof BoatItem boatItem) {
            var entity = ((BoatItemAccessor) boatItem).getChest()
                    ? new ChestBoatEntity(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ())
                    : new BoatEntity(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ());
            entity.setVariant(((BoatItemAccessor) boatItem).getType());
            return entity;
        }
        if (projectileItem instanceof ArrowItem || projectile.isOf(Items.FIREWORK_ROCKET)) {
            return null;
        }
        return null; // TODO: Generic projectile item
    }

    public static void setupProjectile(Entity entity, LivingEntity shooter, ItemStack projectile) {
        if (entity instanceof EyeOfEnderEntity) return;

        if (entity instanceof ExplosiveProjectileEntity) {
            // velocity already handled by constructor
            entity.setPosition(entity.getX(), shooter.getEyeY() - 0.1f, entity.getZ());
            return;
        }

        if (entity instanceof ThrownItemEntity thrownItemEntity) {
            thrownItemEntity.setItem(projectile);
            thrownItemEntity.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), 0.0F, 3.0F, 0.2F);
            return;
        }

        if (entity instanceof ProjectileEntity projectileEntity) {
            projectileEntity.setVelocity(shooter, shooter.getPitch(), shooter.getYaw(), 0.0F, 2.5F, 0.2F);
            return;
        }

        if (entity instanceof FallingBlockEntity fallingBlockEntity && fallingBlockEntity.getBlockState().isIn(BlockTags.ANVIL)) { // TODO: Make this a tag
            entity.setVelocity(getProjectileVel(shooter, 1f));
            return;
        }

        entity.setYaw(shooter.getYaw());
        entity.setVelocity(getProjectileVel(shooter, 1.5f));
    }

    public static boolean shootProjectile(ServerWorld world, LivingEntity shooter, ItemStack projectile) {
        var entity = createProjectile(world, shooter, projectile);
        if (entity == null) return false;
        setupProjectile(entity, shooter, projectile);
        world.spawnEntity(entity);
        return true;
    }

    public static ItemStack getRemainder(ItemStack projectile) {
        if (projectile.getItem() instanceof EntityBucketItem) return Items.BUCKET.getDefaultStack();
        return projectile.getRecipeRemainder();
    }
}
