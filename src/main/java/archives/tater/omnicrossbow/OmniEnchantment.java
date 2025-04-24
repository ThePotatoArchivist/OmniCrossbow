package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.entity.*;
import archives.tater.omnicrossbow.mixin.*;
import archives.tater.omnicrossbow.util.OmniUtil;
import archives.tater.omnicrossbow.util.RaycastUtil;
import moriyashiine.enchancement.common.enchantment.effect.AllowLoadingProjectileEffect;
import moriyashiine.enchancement.common.init.ModComponentTypes;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static archives.tater.omnicrossbow.mixin.FallingBlockEntityInvoker.newFallingBlockEntity;

public class OmniEnchantment {
    private static List<Item> RANDOM_AMMO = null;

    public static boolean isNotDynamic(ItemStack crossbow, ItemStack projectile) {
        return projectile.getItem() instanceof ArrowItem || projectile.isOf(Items.FIREWORK_ROCKET) || projectile.isIn(OmniCrossbow.NON_OMNI_PROJECTILE_TAG)
                || OmniCrossbow.ENCHANCEMENT_INSTALLED && (isBrimstone(projectile) || AllowLoadingProjectileEffect.getItems(crossbow).contains(projectile.getItem()));
    }

    // Assumes enchancement installed, will crash if not
    private static boolean isBrimstone(ItemStack projectile) {
        return projectile.isOf(Items.LAVA_BUCKET) && projectile.getOrDefault(ModComponentTypes.BRIMSTONE_DAMAGE, 0) == Integer.MAX_VALUE;
    }

    public static boolean shouldUnloadImmediate(ItemStack projectile, LivingEntity shooter) {
        return !projectile.isOf(Items.ECHO_SHARD) && !projectile.isOf(Items.NETHER_STAR)
                && !projectile.isOf(Items.FISHING_ROD); // External handling
    }

    public static SoundEvent getSound(ItemStack projectile) {
        if (projectile.isOf(Items.ECHO_SHARD)) return OmniCrossbow.SONIC_PREPARE;
        if (projectile.isOf(Items.NETHER_STAR)) return OmniCrossbow.BEACON_PREPARE;
        if (projectile.isOf(Items.FIRE_CHARGE)) return SoundEvents.ITEM_FIRECHARGE_USE;
        if (projectile.isOf(Items.DRAGON_BREATH)) return SoundEvents.ENTITY_ENDER_DRAGON_SHOOT;
        if (projectile.isOf(Items.WITHER_SKELETON_SKULL)) return SoundEvents.ENTITY_WITHER_SHOOT;
        if (projectile.isOf(Items.BLAZE_POWDER) || projectile.isOf(Items.BLAZE_ROD)) return SoundEvents.ITEM_FIRECHARGE_USE;
        if (projectile.isOf(Items.WIND_CHARGE)) return SoundEvents.ENTITY_WIND_CHARGE_THROW;
        return SoundEvents.ITEM_CROSSBOW_SHOOT;
    }

    // For non-explosive `ProjectileEntity`s launched with `shoot()`
    public static @Nullable SoundEvent getSound(ProjectileEntity projectile) {
        return switch(projectile) {
            case TridentEntity ignored -> SoundEvents.ITEM_TRIDENT_THROW.value();
            case SpyEnderEyeEntity ignored -> SoundEvents.ENTITY_ENDER_EYE_LAUNCH;
            case AbstractWindChargeEntity ignored -> SoundEvents.ENTITY_BREEZE_SHOOT;
            case GrappleFishingHookEntity ignored -> SoundEvents.ENTITY_FISHING_BOBBER_THROW;
            case EmberEntity ignored -> SoundEvents.INTENTIONALLY_EMPTY; // Played separately so as not to play 32 sounds
            default -> null;
        };
    }

    private static void playSoundAt(Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundCategory(), volume, pitch);
    }

    public static int getCooldown(ItemStack projectile) {
        if (projectile.isOf(Items.ECHO_SHARD)) return 80;
        if (projectile.isOf(Items.NETHER_STAR)) return 120;
        if (projectile.isOf(Items.END_CRYSTAL)) return 60;
        if (projectile.isOf(Items.BLAZE_POWDER) || projectile.isOf(Items.BLAZE_ROD) || projectile.isOf(Items.DRAGON_BREATH) || projectile.isOf(Items.WITHER_SKELETON_SKULL)) return 40;
        if (projectile.isOf(Items.FIRE_CHARGE) || projectile.isOf(Items.BREEZE_ROD)) return 20;
        return 0;
    }

    private static Vec3d getProjectileVel(LivingEntity shooter, double length) {
        var target = shooter instanceof CrossbowUser crossbowUser ? crossbowUser.getTarget() : null;
        if (target != null) {
            double dx = target.getX() - shooter.getX();
            double dz = target.getZ() - shooter.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double dy = target.getBodyY(0.3333333333333333) - (shooter.getEyeY() - 0.1) + distance * 0.2F;
            return new Vec3d(CrossbowItemInvoker.invokeCalcVelocity(shooter, new Vec3d(dx, dy, dz), 0)).multiply(length);
        } else {
            return new Vec3d(shooter.getRotationVec(1.0F).toVector3f()).multiply(length);
        }
    }

    private static void shoot(ProjectileEntity projectileEntity, LivingEntity shooter, ItemStack crossbow, float speed) {
        var crossbowItem = crossbow.getItem() instanceof CrossbowItem crossbowItem1 ? crossbowItem1 : (CrossbowItem) Items.CROSSBOW;
        ((CrossbowItemInvoker) crossbowItem).invokeShoot(shooter, projectileEntity, 0, speed, shooter instanceof CrossbowUser ? (14 - shooter.getWorld().getDifficulty().getId() * 4) : 0.2f, 0, shooter instanceof CrossbowUser crossbowUser ? crossbowUser.getTarget() : null);
    }

    @FunctionalInterface
    interface ExplosiveConstructor {
        ExplosiveProjectileEntity create(World world, LivingEntity owner, Vec3d velocity);
    }

    private static ExplosiveProjectileEntity shootExplosive(World world, LivingEntity shooter, double length, ExplosiveConstructor constructor) {
        return constructor.create(world, shooter, getProjectileVel(shooter, length));
    }

    private static @Nullable Entity create(ServerWorld world, EntityType<?> type, LivingEntity shooter, ItemStack projectile, SpawnReason spawnReason) {
        var entity = type.create(world, _entity -> {}, shooter.getBlockPos(), spawnReason, false, false);
        if (entity == null) return null;
        entity.refreshPositionAndAngles(shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ(), shooter.getYaw(), entity.getPitch());
        return entity;
    }

    public static @Nullable Entity createProjectile(ServerWorld world, LivingEntity shooter, ItemStack crossbow, ItemStack projectile) {
        if (isNotDynamic(crossbow, projectile)) return null;
        if (projectile.isIn(OmniCrossbow.DISABLE_ACTION_TAG)) return new GenericItemProjectile(shooter, world);

        var x = shooter.getX();
        var y = shooter.getEyeY() - 0.1f;
        var z = shooter.getZ();

        if (projectile.isOf(Items.EGG)) return new EggEntity(world, shooter);
        if (projectile.isOf(Items.ENDER_PEARL)) return new EnderPearlEntity(world, shooter);
        if (projectile.isOf(Items.SNOWBALL)) return new FreezingSnowballEntity(world, shooter);
        if (projectile.isOf(Items.EXPERIENCE_BOTTLE)) return new ExperienceBottleEntity(world, shooter);
        if (projectile.isOf(Items.SLIME_BALL)) return new SlimeballEntity(shooter, world);
        if (projectile.isOf(Items.TRIDENT)) {
            projectile.damage(1, world, null, (livingEntity) -> {});
            var entity = new TridentEntity(world, shooter, projectile);
            if (shooter instanceof PlayerEntity playerEntity && playerEntity.getAbilities().creativeMode) {
                entity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
            return entity;
        }
        if (projectile.isOf(Items.WIND_CHARGE)) return new LargeWindChargeEntity(world, shooter);
        if (projectile.isOf(Items.ENDER_EYE)) return new SpyEnderEyeEntity(shooter, world);
        if (projectile.isOf(Items.TNT)) return OmniCrossbow.AREALIB_INSTALLED && shooter instanceof ServerPlayerEntity player && player.interactionManager.getGameMode().isBlockBreakingRestricted()
                ? new AreaTntEntity(world, x, y, z, shooter)
                : new TntEntity(world, x, y, z, shooter);
        if (projectile.isOf(Items.WITHER_SKELETON_SKULL)) return shootExplosive(world, shooter, 1f, WitherSkullEntity::new);
        if (projectile.isOf(Items.FIRE_CHARGE)) return shootExplosive(world, shooter, 1f, SmallFireballEntity::new);
        if (projectile.isOf(Items.DRAGON_BREATH)) return shootExplosive(world, shooter, 1f, DragonFireballEntity::new); // TODO small dragon fireball
        if (projectile.isOf(Items.ARMOR_STAND)) return create(world, EntityType.ARMOR_STAND, shooter, projectile, SpawnReason.SPAWN_EGG);
        if (projectile.isOf(Items.FISHING_ROD)) return new GrappleFishingHookEntity(world, shooter);
        if (projectile.isOf(Items.END_CRYSTAL)) return shootExplosive(world, shooter, 0.3f, EndCrystalProjectileEntity::new);
        if (projectile.isOf(Items.ECHO_SHARD)) return new DelayedSonicBoomEntity(world, shooter, crossbow);
        if (projectile.isOf(Items.NETHER_STAR)) return new BeaconLaserEntity(world, shooter, crossbow);

        var projectileItem = projectile.getItem();
        if (projectileItem instanceof ThrowablePotionItem) return new PotionEntity(world, shooter);
        if (projectileItem instanceof BlockItem blockItem && blockItem.getBlock() instanceof FallingBlock fallingBlock) {
            var entity =  newFallingBlockEntity(world, x, y, z, fallingBlock.getDefaultState());
            ((FallingBlockInvoker) fallingBlock).invokeConfigureFallingBlockEntity(entity);
            var nbtComponent = projectile.get(DataComponentTypes.BLOCK_ENTITY_DATA);
            if (nbtComponent != null)
                ((FallingBlockEntityInvoker) entity).setBlockEntityData(nbtComponent.copyNbt());
            return entity;
        }
        if (projectileItem instanceof EntityBucketItem entityBucketItem) return create(world, ((EntityBucketItemAccessor) entityBucketItem).getEntityType(), shooter, projectile, SpawnReason.BUCKET);
        if (projectileItem instanceof SpawnEggItem spawnEggItem && !projectile.isOf(Items.SHULKER_SPAWN_EGG)) return create(world, spawnEggItem.getEntityType(projectile), shooter, projectile, SpawnReason.SPAWN_EGG);
        if (projectileItem instanceof BoatItem boatItem) {
            var entity = ((BoatItemAccessor) boatItem).getChest()
                    ? new ChestBoatEntity(world, x, y, z)
                    : new BoatEntity(world, x, y, z);
            entity.setVariant(((BoatItemAccessor) boatItem).getType());
            return entity;
        }

        if (!projectile.isOf(Items.POTION)) { // Temporary fix, need more robust solution
            var itemId = Registries.ITEM.getId(projectileItem);
            if (Registries.ENTITY_TYPE.containsId(itemId)) {
                var entity = Registries.ENTITY_TYPE.get(itemId).create(world);
                if (entity != null) {
                    if (!entity.isLiving() && !(entity instanceof AbstractDecorationEntity)) {
                        entity.setPosition(shooter.getX(), shooter.getEyeY() - 0.1f, shooter.getZ());
                        if (entity instanceof ProjectileEntity projectileEntity)
                            projectileEntity.setOwner(shooter);
                        return entity;
                    }

                    entity.discard();
                }
            }
        }

        return new GenericItemProjectile(shooter, world);
    }

    public static void setupProjectile(Entity entity, LivingEntity shooter, ItemStack crossbow, ItemStack projectile) {
        if (entity instanceof EyeOfEnderEntity || entity instanceof DelayedShotEntity) {
            playSoundAt(shooter, getSound(projectile), 1f, 1f);
            return;
        }

        if (entity instanceof ExplosiveProjectileEntity && !(entity instanceof AbstractWindChargeEntity)) {
            // velocity already handled by constructor
            entity.setPosition(entity.getX(), shooter.getEyeY() - 0.1f, entity.getZ());
            playSoundAt(shooter, getSound(projectile), 1f, 1f);
            return;
        }

        if (entity instanceof ThrownItemEntity thrownItemEntity) {
            thrownItemEntity.setItem(projectile);
            if (!(thrownItemEntity instanceof GenericItemProjectile)) {
                shoot(thrownItemEntity, shooter, crossbow, 3f);
                return;
            }
        }

        if (entity instanceof EndCrystalProjectileEntity endCrystalProjectile) {
            shoot(endCrystalProjectile, shooter, crossbow, 0.3f);
            return;
        }

        if (entity instanceof SpyEnderEyeEntity spyEnderEyeEntity) {
            shoot(spyEnderEyeEntity, shooter, crossbow, 0.5f);
            return;
        }

        if (entity instanceof AbstractWindChargeEntity windCharge) {
            shoot(windCharge, shooter, crossbow, 2.0f);
            return;
        }

        if (entity instanceof ProjectileEntity projectileEntity) {
            shoot(projectileEntity, shooter, crossbow, 2.5f);
            return;
        }

        playSoundAt(shooter, getSound(projectile), 1f, 1f);

        if (entity instanceof FallingBlockEntity fallingBlockEntity && fallingBlockEntity.getBlockState().isIn(BlockTags.ANVIL)) { // TODO: Make this a tag
            entity.setVelocity(getProjectileVel(shooter, 1f));
            return;
        }

        entity.setYaw(shooter.getYaw());
        entity.setVelocity(getProjectileVel(shooter, 1.5f));
    }

    public static boolean shootProjectile(ServerWorld world, LivingEntity shooter, ItemStack crossbow, ItemStack projectile) {
        if (!projectile.isIn(OmniCrossbow.DISABLE_ACTION_TAG)) {
            if (projectile.isOf(Items.BLAZE_ROD)) {
                shootBlazeRod(world, shooter, crossbow);
                return true;
            }
            if (projectile.isOf(Items.BLAZE_POWDER)) {
                if (!shooter.isOnGround() && shooter.getVelocity().y < 0)
                    shooter.setVelocity(shooter.getVelocity().multiply(1, 0, 1));

                var crossbowItem = crossbow.getItem() instanceof CrossbowItem crossbowItem1 ? crossbowItem1 : (CrossbowItem) Items.CROSSBOW;

                for (int i = 0; i < 12; i++) {
                    var ember = new EmberEntity(shooter, world);

                    ((CrossbowItemInvoker) crossbowItem).invokeShoot(shooter, ember, 0, 1.0f, 32f, 0, shooter instanceof CrossbowUser crossbowUser ? crossbowUser.getTarget() : null);

                    world.spawnEntity(ember);
                }
                playSoundAt(shooter, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.5f, 1.2f);
                playSoundAt(shooter, SoundEvents.ITEM_FIRECHARGE_USE, 1f, 1f);
                var baseVelocity = getProjectileVel(shooter, 1);
                if (shooter.isOnGround())
                    shooter.addVelocity(baseVelocity.multiply(-0.5, -0.5, -0.5));
                else
                    shooter.addVelocity(baseVelocity.multiply(-1.2));
                shooter.velocityModified = true;
                return true;
            }
            if (projectile.isOf(Items.BREEZE_ROD)) {
                var start = shooter.getEyePos().add(0, -0.1, 0);
                var direction = shooter instanceof CrossbowUser crossbowUser ? crossbowUser.getTarget().getEyePos().subtract(start).normalize() : shooter.getRotationVector();
                var end = start.add(direction.multiply(12));
                var hitResult = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, shooter));
                var stop = hitResult.getPos();
                var velocity = direction.y < 0 ? direction.multiply(4, -1, 4) : direction.multiply(4);
                for (var entity : RaycastUtil.pierce(world, start, stop, 1, shooter)) {
                    entity.addVelocity(entity instanceof LivingEntity livingEntity ? velocity.multiply(1 - 0.6 * livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) : velocity);
                    entity.velocityModified = true;
                }
                shooter.addVelocity(direction.multiply(-1));
                shooter.velocityModified = true;
                var distance = stop.subtract(start).length();
                for (int i = 0; i < distance; i++) {
                    var particlePos = start.add(direction.multiply(i)).add(2 * world.random.nextDouble() - 1, 2 * world.random.nextDouble() - 1, 2 * world.random.nextDouble() - 1);
                    world.spawnParticles(ParticleTypes.GUST, particlePos.x, particlePos.y, particlePos.z, 4, 0, 0, 0, 0);
                }
                playSoundAt(shooter, SoundEvents.ENTITY_BREEZE_WIND_BURST.value(), 1.5f, 1f);
                return true;
            }
            if (projectile.isOf(Items.FISHING_ROD) && shooter instanceof Grappler grappler) {
                var hook = grappler.omnicrossbow$getHook();
                if (hook != null) {
                    if (hook.isHooked())
                        hook.unloadCrossbow();
                    else
                        hook.discard();
                    return true;
                }
            }
        }
        var entity = createProjectile(world, shooter, crossbow, projectile);
        if (entity == null) return false;
        setupProjectile(entity, shooter, crossbow, projectile);
        world.spawnEntity(entity);
        return true;
    }

    private static void shootBlazeRod(ServerWorld world, LivingEntity shooter, ItemStack crossbow) {
        var start = shooter.getEyePos().add(0, -0.1, 0);
        var direction = shooter instanceof CrossbowUser crossbowUser ? crossbowUser.getTarget().getEyePos().subtract(start).normalize() : shooter.getRotationVector();
        var current = start;
        var end = start.add(direction.multiply(16));
        var burntPositions = new ArrayList<BlockPos>();
        while (true) {
            var hitResult = world.raycast(new RaycastContext(current, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, shooter));
            current = hitResult.getPos();
            if (hitResult.getType() == HitResult.Type.MISS) break;
            var blockPos = hitResult.getBlockPos();
            var state = world.getBlockState(blockPos);
            if (!state.getFluidState().isEmpty()) {
                if (state.getFluidState().isIn(FluidTags.WATER))
                    world.spawnParticles(ParticleTypes.CLOUD, current.x, current.y + 0.1, current.z, 8, 0, 0, 0, 0);
                break;
            }
            if (OmniUtil.canModifyAt(world, shooter, blockPos) && ((FireBlockInvoker) Blocks.FIRE).invokeGetBurnChance(state) > 0) {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                burntPositions.add(blockPos);
            } else {
                var endPos = blockPos.offset(hitResult.getSide());
                if (OmniUtil.canModifyAt(world, shooter, endPos) && world.getBlockState(endPos).isReplaceable())
                    burntPositions.add(endPos);
                break;
            }
        }
        for (var blockPos : burntPositions) {
            world.setBlockState(blockPos, ((FireBlockInvoker) Blocks.FIRE).invokeGetStateForPosition(world, blockPos));
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 16, 0.25, 0.25, 0.25, 0);
        }
        for (var entity : RaycastUtil.pierce(world, start, current, 0.2, shooter)) {
            entity.damage(world.getDamageSources().create(DamageTypes.IN_FIRE, shooter), 8);
            entity.setOnFireFor(8);
        }
        var distance = current.subtract(start).length();
        for (int i = 0; i < distance * 4; i++) {
            var particlePos = start.add(direction.multiply(i / 4.0));
            world.spawnParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 4, 0, 0, 0, 0.01);
        }
        playSoundAt(shooter, SoundEvents.ITEM_FIRECHARGE_USE, 1f, 1f);
    }

    public static ItemStack getRemainder(ItemStack projectile) {
        if (projectile.getItem() instanceof EntityBucketItem entityBucketItem) return ((BucketItemAccessor) entityBucketItem).getFluid().getBucketItem().getDefaultStack();
        if (!projectile.isIn(OmniCrossbow.HAS_REMAINDER_TAG)) return ItemStack.EMPTY;
        return projectile.getRecipeRemainder();
    }

    @SuppressWarnings("deprecation")
    public static Item getRandomAmmo(Random random) {
        if (RANDOM_AMMO == null)
            RANDOM_AMMO = Registries.ITEM.stream().filter(item -> !item.getRegistryEntry().isIn(OmniCrossbow.NOT_RANDOM_AMMO_TAG) && !(item instanceof SpawnEggItem)).toList();
        return RANDOM_AMMO.get(random.nextInt(RANDOM_AMMO.size()));
    }
}
