package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.entity.CrossbowSnowballEntity;
import archives.tater.omnicrossbow.mixin.*;
import net.minecraft.block.FallingBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
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
        if (projectile.isOf(Items.SNOWBALL)) return new CrossbowSnowballEntity(world, shooter);
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
        if (projectileItem instanceof MinecartItem minecartItem)
            return AbstractMinecartEntity.create(world, shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), ((MinecartItemAccessor) minecartItem).getType());
        if (projectileItem instanceof ArrowItem || projectile.isOf(Items.FIREWORK_ROCKET)) return null;
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

    public static void shootSonicBoom(ServerWorld world, LivingEntity shooter) {
        var start = shooter.getEyePos().add(0, -0.1f, 0);
        var end = start.add(shooter.getRotationVector().multiply(15)); // range = 15
        var hitResult = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, FluidHandling.NONE, shooter));
        var stop = hitResult.getPos();
        var difference = stop.subtract(start);
        var length = difference.length();
        var direction = difference.normalize();

        var areaBox = new Box(start, stop).expand(1);
        var targets = world.getOtherEntities(shooter, areaBox, entity -> {
            var box = entity.getBoundingBox().expand(1 + entity.getTargetingMargin());
            return box.contains(start) || box.raycast(start, stop).isPresent();
        });
        var knockback = direction.multiply(2.5, 0.5, 2.5);
        for (var entity : targets) {
            if (entity instanceof LivingEntity || entity instanceof EndCrystalEntity)
                entity.damage(world.getDamageSources().sonicBoom(shooter), 10);
            var knockbackResist = entity instanceof LivingEntity livingEntity ? livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) : 0;
            entity.addVelocity(knockback.multiply(1 - knockbackResist));
        }
        shooter.addVelocity(knockback.multiply(-0.3));
        shooter.velocityModified = true;

        for (int i = 0; i < length; i++) {
            var node = start.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SONIC_BOOM, node.x, node.y, node.z, 1, 0, 0, 0, 0);
        }
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0F, 1.0F);
    }

    public static boolean shootProjectile(ServerWorld world, LivingEntity shooter, ItemStack projectile) {
        if (projectile.isOf(Items.ECHO_SHARD)) {
            shootSonicBoom(world, shooter);
            return true;
        }
        var entity = createProjectile(world, shooter, projectile);
        if (entity == null) return false;
        setupProjectile(entity, shooter, projectile);
        world.spawnEntity(entity);
        return true;
    }

    public static ItemStack getRemainder(ItemStack projectile) {
        if (projectile.getItem() instanceof EntityBucketItem entityBucketItem) return ((BucketItemAccessor) entityBucketItem).getFluid().getBucketItem().getDefaultStack();
        if (!projectile.isIn(OmniCrossbow.HAS_REMAINDER_TAG)) return ItemStack.EMPTY;
        return projectile.getRecipeRemainder();
    }
}
