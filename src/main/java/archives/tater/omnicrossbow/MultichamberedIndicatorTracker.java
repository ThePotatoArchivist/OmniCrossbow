package archives.tater.omnicrossbow;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

import java.util.List;

public class MultichamberedIndicatorTracker {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(MaxShotsChangedPayload.ID, MaxShotsChangedPayload.CODEC);

        ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previousStack, currentStack) -> {
            if (equipmentSlot.isArmorSlot()
                    || !(livingEntity instanceof ServerPlayerEntity serverPlayer)
                    || !(previousStack.getItem() instanceof CrossbowItem) && !(currentStack.getItem() instanceof CrossbowItem)) return;

            var hand = MultichamberedEnchantment.getPrimaryCrossbowHand(livingEntity);
            if (hand == null) return;
            var crossbow = livingEntity.getStackInHand(hand);
            var maxShots = EnchantmentHelper.getProjectileCount((ServerWorld) livingEntity.getWorld(), crossbow, livingEntity, 1);
            ServerPlayNetworking.send(serverPlayer, new MaxShotsChangedPayload(hand, maxShots));
        });
    }

    public record MaxShotsChangedPayload(Hand hand, int shots) implements CustomPayload {
        private static final List<Hand> HANDS = List.of(Hand.values());
        public static final CustomPayload.Id<MaxShotsChangedPayload> ID = new CustomPayload.Id<>(OmniCrossbow.id("max_shots_changed"));
        public static final PacketCodec<RegistryByteBuf, MaxShotsChangedPayload> CODEC = PacketCodec.tuple(PacketCodecs.indexed(HANDS::get, HANDS::indexOf), MaxShotsChangedPayload::hand, PacketCodecs.INTEGER, MaxShotsChangedPayload::shots, MaxShotsChangedPayload::new);

        @Override
        public Id<MaxShotsChangedPayload> getId() {
            return ID;
        }
    }
}
