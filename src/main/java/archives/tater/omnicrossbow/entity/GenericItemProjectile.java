package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.EntityAccessor;
import archives.tater.omnicrossbow.mixin.LivingEntityAccessor;
import archives.tater.omnicrossbow.mixin.ProjectileEntityAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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
    private @Nullable ItemEntity dropAt(HitResult hitResult) {
        if (getItem().isEmpty()) return null;
        ItemEntity itemEntity = new ItemEntity(this.getWorld(), hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z, getItem());
        itemEntity.setToDefaultPickupDelay();
        itemEntity.setVelocity(0, 0, 0);
        this.getWorld().spawnEntity(itemEntity);
        return itemEntity;
    }

    private FakePlayer createFakePlayer() {
        @Nullable var owner = getOwner();
        var fakePlayer = FakePlayer.get((ServerWorld) getWorld(), owner == null ? new GameProfile(FakePlayer.DEFAULT_UUID, "a crossbow projectile") : new GameProfile(owner.getUuid(), "a crossbow projectile shot by " + owner.getName()));
        fakePlayer.refreshPositionAndAngles(getX(), getY(), getZ(), -getYaw(), getPitch()); // idk why yaw is negative but it's negative
        fakePlayer.setStackInHand(Hand.MAIN_HAND, getItem());
        ((EntityAccessor) fakePlayer).setStandingEyeHeight(0);
        ((LivingEntityAccessor) fakePlayer).invokeGetEquipmentChanges();
        ((LivingEntityAccessor) fakePlayer).setLastAttackedTicks(MathHelper.ceil(fakePlayer.getAttackCooldownProgressPerTick()));
        return fakePlayer;
    }

    private void spawnItemParticles() {
        ((ServerWorld) getWorld()).spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, getItem().copy()), getX(), getY(), getZ(), 6, 0, 0, 0, 0.03);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (getItem().isOf(Items.SLIME_BALL)) { // bouncing has to happen on the client
            var velocity = getVelocity();
            if (velocity.multiply(0.2, 1, 0.2).length() > 0.2) {
                var axis = blockHitResult.getSide().getAxis();
                var newVelocity = velocity.withAxis(axis, -0.5 * velocity.getComponentAlongAxis(axis));
                setVelocity(newVelocity);
                // velocity is processed after collisions, this makes it so that it starts at the hit result next time collision is checked
                setPosition(blockHitResult.getPos().subtract(newVelocity));
                ((ProjectileEntityAccessor) this).setLeftOwner(true);
                playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);
            } else {
                dropAt(blockHitResult);
                discard();
            }
            return;
        }
        if (getWorld().isClient) return;
        customBlockActions(blockHitResult, getItem());
        if (!getItem().isEmpty()) dropAt(blockHitResult);
    }

    private boolean customBlockActions(BlockHitResult blockHitResult, ItemStack stack) {
        var fakePlayer = createFakePlayer();
        var blockPos = blockHitResult.getBlockPos();
        var state = getWorld().getBlockState(blockPos);

        if (stack.isOf(Items.GUNPOWDER)) {
            getWorld().createExplosion(getOwner(), getX(), getY(), getZ(), 1, true, World.ExplosionSourceType.MOB);
            stack.decrement(1);
            return true;
        }

        if (stack.getItem() instanceof BucketItem) {
            var side = blockHitResult.getSide();
            var centerPos = blockPos.offset(side).toCenterPos();
            var pitch = switch (side.getOpposite()) {
                case UP -> -90;
                case DOWN -> 90;
                default -> 0;
            };
            var yaw = side.getOpposite().asRotation();
            fakePlayer.updatePositionAndAngles(centerPos.x, centerPos.y, centerPos.z, yaw, pitch);

            var result = stack.use(getWorld(), fakePlayer, Hand.MAIN_HAND);
            if (result.getResult().isAccepted()) {
                this.setItem(result.getValue());
            }
            return true;
        }

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
        customEntityActions(entityHitResult, getItem());
        if (!getItem().isEmpty()) dropAt(entityHitResult);
    }

    private void customEntityActions(EntityHitResult entityHitResult, ItemStack stack) {
        var world = (ServerWorld) getWorld();
        var entity = entityHitResult.getEntity();

        // Based on Chorus Fruit with tweaks
        if (stack.isOf(Items.CHORUS_FRUIT) && entity instanceof LivingEntity livingEntity) {
            var random = livingEntity.getRandom();
            if (entity.hasVehicle()) entity.stopRiding();
            var currentPos = entity.getPos();

            for (int i = 0; i < 16; i++) {
                var newX = entity.getX() + (random.nextDouble() - 0.5) * 32;
                var newY = MathHelper.clamp(entity.getY() + (random.nextInt(32) - 16), world.getBottomY(), world.getBottomY() + ((ServerWorld) world).getLogicalHeight() - 1);
                var newZ = entity.getZ() + (random.nextDouble() - 0.5) * 32;

                if (!livingEntity.teleport(newX, newY, newZ, true)) continue;

                world.emitGameEvent(GameEvent.TELEPORT, currentPos, GameEvent.Emitter.of(entity));
                SoundEvent soundEvent = entity instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                world.playSound(null, currentPos.x, currentPos.y, currentPos.z, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.playSound(soundEvent, 1.0F, 1.0F);
                break;
            }

            stack.decrement(1);
            return;
        }

        if (stack.isOf(Items.GUNPOWDER)) {
            world.createExplosion(getOwner(), getX(), getY(), getZ(), 1f, true, World.ExplosionSourceType.MOB);
            stack.decrement(1);
            return;
        }

        if (stack.isOf(Items.LIGHTNING_ROD) && entity instanceof LivingEntity && world.isThundering() && world.isSkyVisible(entity.getBlockPos())) {
            LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
            if (lightningEntity != null) {
                lightningEntity.refreshPositionAfterTeleport(entity.getPos());
                lightningEntity.setChanneler(getOwner() instanceof ServerPlayerEntity serverPlayerEntity ? serverPlayerEntity : null);
                world.spawnEntity(lightningEntity);
            }
            world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 5.0F, 1.0F);
            return;
        }

        if (stack.isOf(Items.LEAD) && entity instanceof MobEntity mobEntity && getOwner() instanceof PlayerEntity playerEntity && mobEntity.canBeLeashedBy(playerEntity)) {
            mobEntity.attachLeash(playerEntity, true);
            stack.decrement(1);
            return;
        }

        if (stack.isOf(Items.SLIME_BALL)) {
            var velocity = getVelocity();
            entity.addVelocity(2.5 * velocity.x, ((entity.isOnGround() && velocity.y < 0) ? -0.5 : 1.5) * velocity.y, 2.5 * velocity.z);
            entity.velocityModified = true;
            playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);
            spawnItemParticles();
            stack.decrement(1);
            return;
        }

        if ((stack.isOf(Items.GLOWSTONE_DUST) || stack.isOf(Items.GLOW_BERRIES)) && entity instanceof LivingEntity livingEntity) {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
            spawnItemParticles();
            stack.decrement(1);
            return;
        }

        var item = stack.getItem();

        if (item instanceof PotionItem && !(item instanceof ThrowablePotionItem) && entity instanceof LivingEntity livingEntity) {
            for (var effect : PotionUtil.getPotionEffects(stack))
                if (effect.getEffectType().isInstant())
                    effect.getEffectType().applyInstantEffect(this, getOwner(), livingEntity, effect.getAmplifier(), 1);
                else
                    livingEntity.addStatusEffect(effect);
            playSound(SoundEvents.BLOCK_GLASS_BREAK, 1f, 1f);
            spawnItemParticles();
            // TODO effect particles
            stack.decrement(1);
            return;
        }

        // Potion ingredients
        if (entity instanceof LivingEntity livingEntity)
            for (var potion : new Potion[] {Potions.AWKWARD, Potions.WATER}) {
                var inputStack = PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
                if (BrewingRecipeRegistry.hasRecipe(inputStack, stack)) {
                    var resultEffects = PotionUtil.getPotionEffects(BrewingRecipeRegistry.craft(stack, inputStack));
                    for (var effect : resultEffects)
                        if (effect.getEffectType().isInstant())
                            effect.getEffectType().applyInstantEffect(this, getOwner(), livingEntity, effect.getAmplifier(), 1);
                        else {
                            var newDuration = effect.mapDuration(duration -> duration / 4);
                            livingEntity.addStatusEffect(new StatusEffectInstance(effect.getEffectType(), newDuration, effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
                        }
                    playSound(SoundEvents.ENTITY_GENERIC_EAT, 1f, 1f);
                    spawnItemParticles();
                    stack.decrement(1);
                    return;
                }
            }

        var fakePlayer = createFakePlayer();

        if (entity.interact(fakePlayer, Hand.MAIN_HAND).isAccepted()) return;
        if (entity instanceof LivingEntity livingEntity && getOwner() instanceof PlayerEntity ownerPlayer)
            if (stack.useOnEntity(ownerPlayer, livingEntity, Hand.MAIN_HAND).isAccepted()) return;

        if (item instanceof BlockItem && stack.useOnBlock(new ItemUsageContext(fakePlayer, Hand.MAIN_HAND, new BlockHitResult(entity.getPos(), Direction.UP, entity.getBlockPos().down(), false))).isAccepted()) return;

        // Still use the original player for damaging so that mobs don't aggro on a ghost player
        var damage = (float) fakePlayer.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + EnchantmentHelper.getAttackDamage(stack, entity instanceof LivingEntity livingEntity ? livingEntity.getGroup() : EntityGroup.DEFAULT);
        entity.damage(world.getDamageSources().thrown(this, getOwner()), damage);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient && !getItem().isOf(Items.SLIME_BALL)) discard();
    }
}
