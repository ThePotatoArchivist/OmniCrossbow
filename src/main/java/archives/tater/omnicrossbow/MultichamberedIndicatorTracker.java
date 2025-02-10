package archives.tater.omnicrossbow;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MultichamberedIndicatorTracker {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(MaxShotsChangedPayload.ID, MaxShotsChangedPayload.CODEC);

        ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previousStack, currentStack) -> {
            if (equipmentSlot.isArmorSlot()
                    || !(livingEntity instanceof ServerPlayerEntity serverPlayer)
                    || !previousStack.isOf(Items.CROSSBOW) && !currentStack.isOf(Items.CROSSBOW)) return;

            var crossbow = MultichamberedEnchantment.getPrimaryCrossbow(livingEntity);
            var maxShots = EnchantmentHelper.getProjectileCount((ServerWorld) livingEntity.getWorld(), crossbow, livingEntity, 1);
            ServerPlayNetworking.send(serverPlayer, new MaxShotsChangedPayload(maxShots));
        });
    }

    public record MaxShotsChangedPayload(int shots) implements CustomPayload {
        public static final CustomPayload.Id<MaxShotsChangedPayload> ID = new CustomPayload.Id<>(OmniCrossbow.id("max_shots_changed"));
        public static final PacketCodec<RegistryByteBuf, MaxShotsChangedPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, MaxShotsChangedPayload::shots, MaxShotsChangedPayload::new);

        @Override
        public Id<MaxShotsChangedPayload> getId() {
            return ID;
        }
    }
}
