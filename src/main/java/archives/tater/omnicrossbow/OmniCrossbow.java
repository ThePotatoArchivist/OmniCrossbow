package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import archives.tater.omnicrossbow.networking.SlimeballBouncePayload;
import moriyashiine.enchancement.common.Enchancement;
import archives.tater.omnicrossbow.entity.SlimeballEntity;
import archives.tater.omnicrossbow.util.ClientNetworkingWorkaround;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.value.SetEnchantmentEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmniCrossbow implements ModInitializer {
	public static final String MOD_ID = "omnicrossbow";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

    public static final boolean ENCHANCEMENT_INSTALLED = FabricLoader.getInstance().isModLoaded("enchancement");

	public static final RegistryKey<Enchantment> OMNI = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("omni"));
	public static final RegistryKey<Enchantment> MULTICHAMBERED = RegistryKey.of(RegistryKeys.ENCHANTMENT, id("omni"));

	public static final ComponentType<Unit> CROSSBOW_FULL = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			id("crossbow_full"),
			ComponentType.<Unit>builder().codec(Unit.CODEC).packetCodec(PacketCodec.unit(Unit.INSTANCE)).build()
	);

	public static final Block HONEY_SLICK_BLOCK = Registry.register(Registries.BLOCK, id("honey_slick"), new HoneySlickBlock(AbstractBlock.Settings.create()
			.nonOpaque()
			.strength(0.7f, 0)
			.sounds(BlockSoundGroup.HONEY)
	));

	public static final TagKey<Item> HAS_REMAINDER_TAG = TagKey.of(RegistryKeys.ITEM, id("has_remainder"));
	public static final TagKey<Item> NOT_RANDOM_AMMO_TAG = TagKey.of(RegistryKeys.ITEM, id("not_random_ammo"));
	public static final TagKey<Item> NON_OMNI_PROJECTILE_TAG = TagKey.of(RegistryKeys.ITEM, id("non_omni_projectile"));

	public static final RegistryKey<DamageType> BEACON_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("beacon"));

	public static final TagKey<EntityType<?>> CAN_EQUIP_TAG = TagKey.of(RegistryKeys.ENTITY_TYPE, id("can_equip"));

	private static SoundEvent of(Identifier id) {
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static final SoundEvent BEACON_PREPARE = of(id("projectile.beacon.prepare"));
	public static final SoundEvent BEACON_FIRE = of(id("projectile.beacon.fire"));
	public static final SoundEvent SONIC_PREPARE = of(id("projectile.sonic.prepare"));
	public static final SoundEvent SONIC_FIRE = of(id("projectile.sonic.fire"));
	public static final SoundEvent END_CRYSTAL_HIT = of(id("projectile.endcrystal.hit"));

	public static ClientNetworkingWorkaround CLIENT_NETWORKING = null;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		OmniCrossbowEnchantmentEffects.register();
		OmniCrossbowEntities.register();
		MultichamberedIndicatorTracker.register();
		if (ENCHANCEMENT_INSTALLED) {
			//noinspection OptionalGetWithoutIsPresent
			ResourceManagerHelper.registerBuiltinResourcePack(id("enchancement_compat"),
					FabricLoader.getInstance().getModContainer(MOD_ID).get(),
					Text.translatable("resourcepack.omnicrossbow.enchancement_compat"),
					ResourcePackActivationType.ALWAYS_ENABLED);
			var SCATTER = Identifier.of(Enchancement.MOD_ID, "scatter");
			EnchantmentEvents.MODIFY.register((registryKey, builder, enchantmentSource) -> {
				if (!enchantmentSource.isBuiltin()) return;
                if (registryKey.getValue().equals(SCATTER)) {
					builder.addEffect(EnchantmentEffectComponentTypes.PROJECTILE_COUNT, new SetEnchantmentEffect(EnchantmentLevelBasedValue.constant(6)));
					builder.addEffect(OmniCrossbowEnchantmentEffects.ONE_PROJECTILE_AT_TIME);
				}
			});
		}
		PayloadTypeRegistry.playC2S().register(SlimeballBouncePayload.ID, SlimeballBouncePayload.CODEC);
		SlimeballEntity.registerPacketReceiver();
	}
}
