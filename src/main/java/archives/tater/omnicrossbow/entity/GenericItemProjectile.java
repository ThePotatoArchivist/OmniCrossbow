package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.HoneySlickBlock;
import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.mixin.EntityAccessor;
import archives.tater.omnicrossbow.mixin.LivingEntityAccessor;
import archives.tater.omnicrossbow.mixin.PlayerEntityInvoker;
import archives.tater.omnicrossbow.util.OmniUtil;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        return Items.AIR;
    }

    @Deprecated
    private ItemStack getItem() {
        return getStack();
    }

    // Call server side
    private @Nullable ItemEntity dropAt(HitResult hitResult, boolean randomVelocity) {
        if (getItem().isEmpty()) return null;
        ItemEntity itemEntity = new ItemEntity(this.getWorld(), hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z, getItem());
        itemEntity.setToDefaultPickupDelay();
        if (!randomVelocity)
            itemEntity.setVelocity(0, 0, 0);
        this.getWorld().spawnEntity(itemEntity);
        return itemEntity;
    }

    @Override
    public boolean canModifyAt(World world, BlockPos pos) {
        return super.canModifyAt(world, pos) && OmniUtil.modifyNotRestrictedAt(world, getOwner(), pos);
    }

    private FakePlayer createFakePlayer() {
        @Nullable var owner = getOwner();
        var fakePlayer = FakePlayer.get((ServerWorld) getWorld(), owner == null ? new GameProfile(FakePlayer.DEFAULT_UUID, "a crossbow projectile") : new GameProfile(owner.getUuid(), "a crossbow projectile shot by " + owner.getName()));
        fakePlayer.refreshPositionAndAngles(getX(), getY(), getZ(), -getYaw(), getPitch()); // idk why yaw is negative but it's negative
        fakePlayer.setVelocity(getVelocity());
        fakePlayer.setStackInHand(Hand.MAIN_HAND, getItem());
        ((EntityAccessor) fakePlayer).setStandingEyeHeight(0);
        ((LivingEntityAccessor) fakePlayer).invokeGetEquipmentChanges();
        ((LivingEntityAccessor) fakePlayer).setLastAttackedTicks(MathHelper.ceil(fakePlayer.getAttackCooldownProgressPerTick()));
        if (owner instanceof ServerPlayerEntity serverPlayer)
            fakePlayer.changeGameMode(serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE ? GameMode.ADVENTURE : GameMode.SURVIVAL);
        return fakePlayer;
    }

    private void handleItemsFrom(PlayerEntity player) {
        setItem(player.getStackInHand(Hand.MAIN_HAND));
        player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        player.getInventory().dropAll();
    }

    @Override
    public void handleStatus(byte status) {
        super.handleStatus(status);

        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            var particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, getItem());

            for (int i = 0; i < 6; i++)
                getWorld().addParticle(particleEffect,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        0.3 * random.nextDouble() - 0.15,
                        0.3 * random.nextDouble() - 0.15,
                        0.3 * random.nextDouble() - 0.15
                );
        }

        if (status == EntityStatuses.EXPLODE_FIREWORK_CLIENT) {
            var explosion = getStack().get(DataComponentTypes.FIREWORK_EXPLOSION);
            if (explosion == null) return;
            var vec3d = this.getVelocity();
            this.getWorld().addFireworkParticle(this.getX(), this.getY(), this.getZ(), vec3d.x, vec3d.y, vec3d.z, List.of(explosion));
        }
    }

    private void spawnItemParticles() {
        if (getWorld().isClient) return;
        getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (getWorld().isClient) return;
        var success = !getItem().isIn(OmniCrossbow.DISABLE_ACTION_TAG) && customBlockActions(blockHitResult, getItem(), null);
        if (!getItem().isEmpty()) dropAt(blockHitResult, success);
    }

    private boolean customBlockActions(BlockHitResult blockHitResult, ItemStack stack, @Nullable FakePlayer reusePlayer) {
        var blockPos = blockHitResult.getBlockPos();
        var world = getWorld();
        var state = world.getBlockState(blockPos);

        if (stack.isOf(Items.GUNPOWDER)) {
            OmniUtil.areaCheckExplosion(world, this, getOwner(), 1);
            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.FIREWORK_STAR) && stack.contains(DataComponentTypes.FIREWORK_EXPLOSION)) {
            world.sendEntityStatus(this, EntityStatuses.EXPLODE_FIREWORK_CLIENT);
            var pos = getPos();

            for (LivingEntity livingEntity : this.getWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(5.0))) {
                if (livingEntity.squaredDistanceTo(pos) <= 25.0) {
                    boolean hit = false;

                    for (int i = 0; i < 2; i++) {
                        Vec3d entityPos = new Vec3d(livingEntity.getX(), livingEntity.getBodyY(0.5 * i), livingEntity.getZ());
                        HitResult hitResult = this.getWorld().raycast(new RaycastContext(pos, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                        if (hitResult.getType() == HitResult.Type.MISS) {
                            hit = true;
                            break;
                        }
                    }

                    if (hit) {
                        float g = 7 * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
                        livingEntity.damage(new DamageSource(getDamageSources().registry.entryOf(DamageTypes.FIREWORKS), this, this.getOwner()), g);
                    }
                }
            }

            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.HONEY_BOTTLE)) {
            var blockState = OmniCrossbow.HONEY_SLICK_BLOCK.getDefaultState().with(HoneySlickBlock.FACING, blockHitResult.getSide().getOpposite());
            var placePos = blockPos.offset(blockHitResult.getSide());
            if (canModifyAt(world, placePos) && world.getBlockState(placePos).isReplaceable() && blockState.canPlaceAt(world, placePos)) {
                world.setBlockState(placePos, blockState);
                playSound(SoundEvents.BLOCK_GLASS_BREAK, 0.3f, 1f);
                playSound(SoundEvents.BLOCK_HONEY_BLOCK_PLACE, 1f, 1f);
                spawnItemParticles();
                stack.decrement(1);
                return true;
            }
        }

        if (stack.isOf(Items.AMETHYST_SHARD)) {
            playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1f);
            spawnItemParticles();
            stack.decrement(1);
            return true;
        }

        if (stack.isIn(ConventionalItemTags.DYES) && canModifyAt(world, blockPos) && !state.hasBlockEntity() && state.getBlock().asItem() != Items.AIR) {
            var blockStack = state.getBlock().asItem().getDefaultStack();
            for (var items : new List[] {
                List.of(stack, blockStack),
                List.of(blockStack, blockStack, blockStack, blockStack, stack, blockStack, blockStack, blockStack, blockStack)
            }) {
                var inputStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
                for (int i = 0; i < items.size(); i++)
                    inputStacks.set(i, (ItemStack) items.get(i));
                var recipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, CraftingRecipeInput.create(3, 3, inputStacks), world);
                if (recipe.isEmpty()) continue;
                var resultStack = recipe.get().value().getResult(world.getRegistryManager());
                if (!(resultStack.getItem() instanceof BlockItem resultBlockItem)) continue;
                stack.decrement(1);
                if (state.getBlock() instanceof BedBlock) {
                    var direction = BedBlock.getOppositePartDirection(state);
                    world.setBlockState(blockPos.offset(direction), resultBlockItem.getBlock().getStateWithProperties(world.getBlockState(blockPos.offset(direction))), Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD | Block.FORCE_STATE);
                }
                world.setBlockState(blockPos, resultBlockItem.getBlock().getStateWithProperties(state));
                playSound(SoundEvents.ITEM_DYE_USE, 1f, 1f);
                return true;
            }
        }

        var fakePlayer = reusePlayer == null ? createFakePlayer() : reusePlayer;

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

            var result = stack.use(world, fakePlayer, Hand.MAIN_HAND);
            if (result.getResult().isAccepted()) {
                this.setItem(result.getValue());
            }
            return true;
        }

        if (isSuitableTool(stack, blockPos, state, fakePlayer) && fakePlayer.interactionManager.tryBreakBlock(blockPos)) return true;
        var offsetPos = blockPos.offset(blockHitResult.getSide());
        if (isSuitableTool(stack, offsetPos, state, fakePlayer) && fakePlayer.interactionManager.tryBreakBlock(offsetPos)) return true;

        if (stack.isOf(Items.NOTE_BLOCK))
            for (int i = 0; i < 12; i++)
                world.playSound(null, blockPos, SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), getSoundCategory(), 2f, NoteBlock.getNotePitch(random.nextBetween(-12, 24)));

        if (stack.isOf(Items.LIGHTNING_ROD) && stack.useOnBlock(new ItemUsageContext(world, fakePlayer, Hand.MAIN_HAND, stack, blockHitResult)).isAccepted()) {
            if (world.getBlockState(offsetPos).isOf(Blocks.LIGHTNING_ROD) && world.isThundering() && world.isSkyVisible(offsetPos)) {
                var lightningBolt = EntityType.LIGHTNING_BOLT.create(world);
                if (lightningBolt != null) {
                    lightningBolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(offsetPos.up()));
                    lightningBolt.setChanneler(getOwner() instanceof ServerPlayerEntity serverPlayerEntity ? serverPlayerEntity : null);
                    world.spawnEntity(lightningBolt);
                }
                world.playSound(null, offsetPos, SoundEvents.ITEM_TRIDENT_THUNDER.value(), SoundCategory.WEATHER, 5.0F, 1.0F);
            }
            return true;
        }

        if (state.onUseWithItem(stack, world, fakePlayer, Hand.MAIN_HAND, blockHitResult).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }
        if (stack.useOnBlock(new ItemUsageContext(world, fakePlayer, Hand.MAIN_HAND, stack, blockHitResult)).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }
        var offsetHit = new BlockHitResult(blockHitResult.getPos(), blockHitResult.getSide(), offsetPos, true);
        if (world.getBlockState(offsetPos).onUseWithItem(stack, world, fakePlayer, Hand.MAIN_HAND, offsetHit).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }
        if (stack.useOnBlock(new ItemUsageContext(world, fakePlayer, Hand.MAIN_HAND, stack, offsetHit)).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }

        return false;
    }

    private boolean isSuitableTool(ItemStack tool, BlockPos blockPos, BlockState state, PlayerEntity fakePlayer) {
        return tool.isSuitableFor(state) || tool.isIn(ConventionalItemTags.SHEAR_TOOLS) && state.isIn(BlockTags.LEAVES) || (tool.getItem() instanceof MiningToolItem && !state.isIn(BlockTags.AXE_MINEABLE) && !state.isIn(BlockTags.HOE_MINEABLE) && !state.isIn(BlockTags.PICKAXE_MINEABLE) && !state.isIn(BlockTags.PICKAXE_MINEABLE) & state.calcBlockBreakingDelta(fakePlayer, getWorld(), blockPos) >= 0.005);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (getWorld().isClient) return;
        if (!getItem().isIn(OmniCrossbow.DISABLE_ACTION_TAG) && customEntityActions(entityHitResult, getItem())) {
            if (!getItem().isEmpty()) dropAt(entityHitResult, true);
        }
    }

    private boolean customEntityActions(EntityHitResult entityHitResult, ItemStack stack) {
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
            return true;
        }

        if (stack.isOf(Items.GUNPOWDER)) {
            world.createExplosion(getOwner(), entity.getX(), getY(), entity.getZ(), 1f, true, World.ExplosionSourceType.MOB);
            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.LIGHTNING_ROD) && entity instanceof LivingEntity && world.isThundering() && world.isSkyVisible(entity.getBlockPos())) {
            var lightningBolt = EntityType.LIGHTNING_BOLT.create(world);
            if (lightningBolt != null) {
                lightningBolt.refreshPositionAfterTeleport(entity.getPos());
                lightningBolt.setChanneler(getOwner() instanceof ServerPlayerEntity serverPlayerEntity ? serverPlayerEntity : null);
                world.spawnEntity(lightningBolt);
            }
            world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_TRIDENT_THUNDER.value(), SoundCategory.WEATHER, 5.0F, 1.0F);
            return true;
        }

        if (stack.isOf(Items.LEAD) && entity instanceof MobEntity mobEntity && getOwner() instanceof PlayerEntity playerEntity && mobEntity.canLeashAttachTo()) {
            mobEntity.attachLeash(playerEntity, true);
            stack.decrement(1);
            return true;
        }

        if ((stack.isOf(Items.GLOWSTONE_DUST) || stack.isOf(Items.GLOW_BERRIES) || stack.isOf(Items.GLOW_INK_SAC)) && entity instanceof LivingEntity livingEntity) {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
            spawnItemParticles();
            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.AMETHYST_SHARD) && entity.damage(world.getDamageSources().mobProjectile(this, getOwner() instanceof LivingEntity livingEntity ? livingEntity : null), 8)) {
            playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1f, 1f);
            spawnItemParticles();
            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.OMINOUS_BOTTLE) && entity instanceof LivingEntity livingEntity) {
            playSound(SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE, 1.0F, 1.0F);
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN, 120000, stack.getOrDefault(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER, 0), false, false, true));
            spawnItemParticles();
            stack.decrement(1);
            return true;
        }

        if (stack.isOf(Items.MILK_BUCKET) && entity instanceof LivingEntity livingEntity) {
            var effects = livingEntity.getStatusEffects();
            if (!effects.isEmpty())
                effects.stream()
                        .skip(random.nextInt(effects.size())).findFirst()
                        .ifPresent(effect -> livingEntity.removeStatusEffect(effect.getEffectType()));
            playSound(SoundEvents.ENTITY_WANDERING_TRADER_DRINK_MILK, 1f, 1f);
            setItem(Items.BUCKET.getDefaultStack());
            return true;
        }

        if (stack.isOf(Items.FEATHER)) return true;

        if (entity instanceof LivingEntity livingEntity && (livingEntity.getType().isIn(OmniCrossbow.CAN_EQUIP_TAG) || livingEntity.canEquip(stack))) {
            var slot = livingEntity.getPreferredEquipmentSlot(stack);
            if (slot != EquipmentSlot.MAINHAND) {
                var equippedStack = livingEntity.getEquippedStack(slot);
                if (!EnchantmentHelper.hasAnyEnchantmentsWith(equippedStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
                    livingEntity.dropStack(equippedStack);
                    livingEntity.equipStack(slot, stack.copy());
                    stack.decrement(1);
                    return true;
                }
            }
        }

        var item = stack.getItem();

        if (item instanceof PotionItem && !(item instanceof ThrowablePotionItem) && entity instanceof LivingEntity livingEntity) {
            for (var effect : stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects())
                if (effect.getEffectType().value().isInstant())
                    effect.getEffectType().value().applyInstantEffect(this, getOwner(), livingEntity, effect.getAmplifier(), 1);
                else
                    livingEntity.addStatusEffect(effect);
            playSound(SoundEvents.BLOCK_GLASS_BREAK, 1f, 1f);
            spawnItemParticles();
            // TODO effect particles
            stack.decrement(1);
            return true;
        }

        // Foods
        if (entity instanceof LivingEntity livingEntity && !stack.isOf(Items.HONEY_BOTTLE)) {
            var food = stack.get(DataComponentTypes.FOOD);
            if (food != null) {
                var count = stack.getCount();
                setItem(stack.finishUsing(world, livingEntity));
                if (!(entity instanceof PlayerEntity))
                    food.usingConvertsTo().ifPresent(this::dropStack);
                if (stack.getCount() == count) // Doesn't automatically decrement if not creative mode
                    stack.decrement(1);
                spawnItemParticles();
                return true;
            }
        }

        // Potion ingredients
        if (entity instanceof LivingEntity livingEntity && !stack.isOf(Items.COBWEB)) // TODO make this not hardcoded
            for (var potion : new RegistryEntry[] {Potions.AWKWARD, Potions.WATER}) {
                @SuppressWarnings("unchecked")
                var inputStack = PotionContentsComponent.createStack(Items.POTION, potion);

                if (world.getBrewingRecipeRegistry().hasRecipe(inputStack, stack)) {
                    var resultEffects = world.getBrewingRecipeRegistry().craft(stack, inputStack).getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects();
                    for (var effect : resultEffects)
                        if (effect.getEffectType().value().isInstant())
                            effect.getEffectType().value().applyInstantEffect(this, getOwner(), livingEntity, effect.getAmplifier(), 1);
                        else {
                            var newDuration = effect.mapDuration(duration -> duration / 4);
                            livingEntity.addStatusEffect(new StatusEffectInstance(effect.getEffectType(), newDuration, effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
                        }
                    playSound(SoundEvents.ENTITY_GENERIC_EAT, 1f, 1f);
                    spawnItemParticles();
                    stack.decrement(1);
                    return true;
                }
            }

        var fakePlayer = createFakePlayer();

        if (!(entity instanceof MerchantEntity) && entity.interact(fakePlayer, Hand.MAIN_HAND).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }
        if (entity instanceof LivingEntity livingEntity && stack.useOnEntity(fakePlayer, livingEntity, Hand.MAIN_HAND).isAccepted()) {
            handleItemsFrom(fakePlayer);
            return true;
        }

        if (customBlockActions(new BlockHitResult(entity.getPos(), Direction.UP, entity.getBlockPos().down(), false), stack, fakePlayer)) return true;

        // Still use the original player for damaging so that mobs don't aggro on a ghost player
        var source = world.getDamageSources().thrown(this, getOwner());
        var damage = ((PlayerEntityInvoker) fakePlayer).invokeGetDamageAgainst(entity, (float) fakePlayer.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE), source);
        if (!entity.damage(source, damage)) return false;
        if (entity instanceof LivingEntity livingEntity)
            stack.postHit(livingEntity, fakePlayer);

        if (stack.isOf(Items.BELL))
            playSound(SoundEvents.BLOCK_BELL_USE, 1f, 1f);
        if (stack.isOf(Items.NOTE_BLOCK))
            for (int i = 0; i < 12; i++)
                playSound(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 2f, NoteBlock.getNotePitch(random.nextBetween(-12, 24)));
        return true;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!getWorld().isClient) discard();
    }
}
