package archives.tater.omnicrossbow;

import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmniCrossbow implements ModInitializer {
	public static final String MOD_ID = "omnicrossbow";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Enchantment OMNI = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "omni"), new OmniEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));

	public static final TagKey<Item> HAS_REMAINDER_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "has_remainder"));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		OmniCrossbowEntities.register();
	}
}
