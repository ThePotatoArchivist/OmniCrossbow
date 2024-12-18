package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.util.RaycastUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class DelayedSonicBoomEntity extends DelayedShotEntity {
    public DelayedSonicBoomEntity(EntityType<? extends DelayedShotEntity> type, World world) {
        super(type, world);
    }

    public DelayedSonicBoomEntity(World world, LivingEntity owner, ItemStack launcher) {
        super(OmniCrossbowEntities.DELAYED_SONIC_BOOM, world, owner, launcher, 32);
    }

    @Override
    protected void onShoot() {
        if (owner == null) return;
        var world = (ServerWorld) getWorld();

        var start = owner.getEyePos().add(0, -0.1f, 0);
        var end = start.add(owner.getRotationVector().multiply(15)); // range = 15
        var stop = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner)).getPos();
        var difference = stop.subtract(start);
        var direction = difference.normalize();

        var knockback = direction.multiply(2.5, 0.5, 2.5);
        var targets = RaycastUtil.pierce(world, start, stop, 1f, owner, null);
        for (var entity : targets) {
            if (entity instanceof LivingEntity || entity instanceof EndCrystalEntity)
                entity.damage(world.getDamageSources().sonicBoom(owner), 10);
            var knockbackResist = entity instanceof LivingEntity livingEntity ? livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE) : 0;
            entity.addVelocity(knockback.multiply(1 - knockbackResist));
        }
        owner.addVelocity(knockback.multiply(-0.5));
        owner.velocityModified = true;

        var length = difference.length();
        for (int i = 0; i < length; i++) {
            var node = start.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SONIC_BOOM, node.x, node.y, node.z, 1, 0, 0, 0, 0);
        }

        if (owner instanceof PlayerEntity playerEntity) playerEntity.getItemCooldownManager().set(launcher.getItem(), 80);
        unloadCrossbow();
        discard();
    }

    @Override
    protected SoundEvent getShootSound() {
        return OmniCrossbow.SONIC_FIRE;
    }

    // Same as MarkerEntity

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        throw new IllegalStateException("Delayed Sonic Boom should never be sent");
    }
}
