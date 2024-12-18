package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.mixin.ProjectileEntityAccessor;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class SlimeballEntity extends ThrownItemEntity {
    public SlimeballEntity(EntityType<? extends SlimeballEntity> type, World world) {
        super(type, world);
    }

    public SlimeballEntity(double x, double y, double z, World world) {
        super(OmniCrossbowEntities.SLIME_BALL, x, y, z, world);
    }

    public SlimeballEntity(LivingEntity owner, World world) {
        super(OmniCrossbowEntities.SLIME_BALL, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        var velocity = getVelocity();

        if (!(velocity.multiply(0.2, 1, 0.2).length() > 0.2)) {
            var pos = blockHitResult.getPos();
            var itemEntity = new ItemEntity(this.getWorld(), pos.x, pos.y, pos.z, getStack());
            itemEntity.setToDefaultPickupDelay();
            itemEntity.setVelocity(0, 0, 0);
            this.getWorld().spawnEntity(itemEntity);
            discard();
            return;
        }

        var axis = blockHitResult.getSide().getAxis();
        var newVelocity = velocity.withAxis(axis, -0.5 * velocity.getComponentAlongAxis(axis));
        setVelocity(newVelocity);
        // velocity is processed after collisions, this makes it so that it starts at the hit result next time collision is checked
        setPosition(blockHitResult.getPos().subtract(newVelocity));
        ((ProjectileEntityAccessor) this).setLeftOwner(true);
        playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        var entity = entityHitResult.getEntity();
        var velocity = getVelocity();
        entity.addVelocity(2.5 * velocity.x, ((entity.isOnGround() && velocity.y < 0) ? -0.5 : 1.5) * velocity.y, 2.5 * velocity.z);
        entity.velocityModified = true;
        playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);

        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
            this.discard();
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) return;

        var particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, getStack());

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
}
