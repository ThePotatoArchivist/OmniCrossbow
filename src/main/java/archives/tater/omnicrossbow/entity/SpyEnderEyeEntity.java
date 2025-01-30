package archives.tater.omnicrossbow.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class SpyEnderEyeEntity extends ThrownItemEntity {
    private int lifespan = 0;
    private float clientPitch = 0f;
    private float clientYaw = 0f;

    public static final int MAX_LIFESPAN = 200;

    public SpyEnderEyeEntity(EntityType<? extends SpyEnderEyeEntity> type, World world) {
        super(type, world);
    }

    public SpyEnderEyeEntity(LivingEntity owner, World world) {
        super(OmniCrossbowEntities.SPY_ENDER_EYE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_EYE;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void updateRotation() {
    }

    @Override
    public float getYaw() {
        return clientYaw;
    }

    @Override
    public float getPitch() {
        return clientPitch;
    }

    @Override
    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        float deltaPitch = (float)cursorDeltaY * 0.15F;
        float deltaYaw = (float)cursorDeltaX * 0.15F;
        clientPitch = MathHelper.clamp(this.getPitch() + deltaPitch, -90.0F, 90.0F);
        clientYaw = this.getYaw() + deltaYaw;
        this.prevPitch = MathHelper.clamp(this.prevPitch + deltaPitch, -90.0F, 90.0F);
        this.prevYaw += deltaYaw;
    }

    @Override
    public void tick() {
        super.tick();

        setVelocity(getVelocity().multiply(0.95));

        if (getWorld().isClient) {
            var velocity = getVelocity();
            this.getWorld().addParticle(
                    ParticleTypes.PORTAL,
                    getX() + velocity.x - velocity.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    getY() + velocity.y - velocity.y * 0.25 - 0.5,
                    getZ() + velocity.z - velocity.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );
        }

        if (lifespan == 0) {
            var owner = getOwner();
            if (owner instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.setCameraEntity(this);
            }
        }

        lifespan++;
        if (!getWorld().isClient && lifespan > MAX_LIFESPAN) {
            playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
            dropStack(getStack());
            discard();
        }
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        var owner = getOwner();
        if (owner == null) return;
        clientYaw = wrapDegrees(owner.getYaw() + 180);
        clientPitch = wrapDegrees(-owner.getPitch());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Lifespan", lifespan);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        lifespan = nbt.getInt("Lifespan");
    }
}
