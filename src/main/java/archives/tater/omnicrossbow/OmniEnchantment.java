package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.mixin.FallingBlockInvoker;
import net.minecraft.block.FallingBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.item.*;
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

    public static @Nullable Entity createProjectile(World world, LivingEntity shooter, ItemStack projectile) {
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

        var projectileItem = projectile.getItem();
        if (projectileItem instanceof ThrowablePotionItem) {
            return new PotionEntity(world, shooter);
        }
        if (projectileItem instanceof BlockItem blockItem && blockItem.getBlock() instanceof FallingBlock fallingBlock) {
            var entity =  newFallingBlockEntity(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), fallingBlock.getDefaultState());
            ((FallingBlockInvoker) fallingBlock).invokeConfigureFallingBlockEntity(entity);
            return entity;
        }
        if (projectileItem instanceof SpawnEggItem spawnEggItem) {
            var entity = spawnEggItem.getEntityType(null).create((ServerWorld) world, projectile.getNbt(), null, shooter.getBlockPos(), SpawnReason.SPAWN_EGG, false, false);
            if (entity == null) return null;
            entity.refreshPositionAndAngles(shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), shooter.headYaw, shooter.getPitch());
            return entity;
        }
        if (projectileItem instanceof ArrowItem || projectile.isOf(Items.FIREWORK_ROCKET)) {
            return null;
        }
        return null; // TODO: Generic projectile item
    }

    public static void setupProjectile(Entity entity, World world, LivingEntity shooter, ItemStack projectile) {
        if (entity instanceof EyeOfEnderEntity) return;

        if (entity instanceof ExplosiveProjectileEntity) {
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

        entity.setVelocity(getProjectileVel(shooter, 1f));
    }

    public static boolean shootProjectile(World world, LivingEntity shooter, ItemStack projectile) {
        var entity = createProjectile(world, shooter, projectile);
        if (entity == null) return false;
        setupProjectile(entity, world, shooter, projectile);
        world.spawnEntity(entity);
        return true;
    }

}
