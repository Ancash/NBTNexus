package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.ALTERNATE_COLOR_CODE;
import static de.ancash.nbtnexus.Tags.ATTRIBUTES_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_AMOUNT_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_NAME_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_OPERATION_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_SLOT_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_TYPE_TAG;
import static de.ancash.nbtnexus.Tags.ATTRIBUTE_UUID_TAG;
import static de.ancash.nbtnexus.Tags.CUSTOM_MODEL_DATA;
import static de.ancash.nbtnexus.Tags.DAMAGE_TAG;
import static de.ancash.nbtnexus.Tags.DISPLAYNAME_TAG;
import static de.ancash.nbtnexus.Tags.DISPLAY_TAG;
import static de.ancash.nbtnexus.Tags.ENCHANTMENTS_TAG;
import static de.ancash.nbtnexus.Tags.ENCHANTMENT_LEVEL_TAG;
import static de.ancash.nbtnexus.Tags.ENCHANTMENT_TYPE_TAG;
import static de.ancash.nbtnexus.Tags.LOCALIZED_NAME_TAG;
import static de.ancash.nbtnexus.Tags.LORE_TAG;
import static de.ancash.nbtnexus.Tags.REPAIR_COST_TAG;
import static de.ancash.nbtnexus.Tags.UNSPECIFIC_META_TAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import de.ancash.minecraft.cryptomorin.xseries.XEnchantment;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import net.md_5.bungee.api.ChatColor;

public class SimpleMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final SimpleMetaSerDe INSTANCE = new SimpleMetaSerDe();

	SimpleMetaSerDe() {
	}

	public String translateChatColor(String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++)
			if (b[i] == ChatColor.COLOR_CHAR && ChatColor.ALL_CODES.indexOf(b[i + 1]) > -1)
				b[i] = ALTERNATE_COLOR_CODE;
		return new String(b);
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> serMeta = new HashMap<>();
		if (meta.hasLore()) {
			serMeta.put(LORE_TAG, meta.getLore().stream().map(this::translateChatColor).collect(Collectors.toList()));
			meta.setLore(null);
		}
		if (meta.hasDisplayName()) {
			serMeta.put(DISPLAYNAME_TAG, translateChatColor(meta.getDisplayName()));
			meta.setDisplayName(null);
		}
		if (meta.hasLocalizedName()) {
			serMeta.put(LOCALIZED_NAME_TAG, translateChatColor(meta.getLocalizedName()));
			meta.setLocalizedName(null);
		}
		if (meta.hasCustomModelData()) {
			serMeta.put(CUSTOM_MODEL_DATA, meta.getCustomModelData());
			meta.setCustomModelData(null);
		}

		if (meta instanceof Damageable) {
			Damageable damageable = (Damageable) meta;
			if (damageable.hasDamage())
				serMeta.put(DAMAGE_TAG, damageable.getDamage());
			damageable.setDamage(0);
		}

		if (meta instanceof Repairable) {
			Repairable repairable = (Repairable) meta;
			if (repairable.hasRepairCost())
				serMeta.put(REPAIR_COST_TAG, repairable.getRepairCost());
			repairable.setRepairCost(0);
		}
		map.put(DISPLAY_TAG, serMeta);
		item.setItemMeta(meta);
		if (!item.getEnchantments().isEmpty()) {
			map.put(ENCHANTMENTS_TAG, serializeEnchantments(item));
			item.getEnchantments().keySet().forEach(item::removeEnchantment);
		}
		if (meta.hasAttributeModifiers()) {
			List<Map<String, Object>> attributes = new ArrayList<>();
			for (Entry<Attribute, AttributeModifier> modifier : meta.getAttributeModifiers().entries()) {
				Map<String, Object> ser = new HashMap<>();
				ser.put(ATTRIBUTE_TYPE_TAG, modifier.getKey().name());
				ser.put(ATTRIBUTE_NAME_TAG, modifier.getValue().getName());
				ser.put(ATTRIBUTE_AMOUNT_TAG, modifier.getValue().getAmount());
				ser.put(ATTRIBUTE_OPERATION_TAG, modifier.getValue().getOperation().name());
				ser.put(ATTRIBUTE_UUID_TAG, modifier.getValue().getUniqueId().toString());
				if (modifier.getValue().getSlot() != null)
					ser.put(ATTRIBUTE_SLOT_TAG, modifier.getValue().getSlot().name());
				attributes.add(ser);
			}
			map.put(ATTRIBUTES_TAG, attributes);
			Arrays.stream(Attribute.values()).forEach(meta::removeAttributeModifier);
		}
		item.setItemMeta(meta);
		return map;
	}

	protected List<Map<String, Object>> serializeEnchantments(ItemStack item) {
		Map<Enchantment, Integer> enchs = new HashMap<>();
		item.getEnchantments().forEach(enchs::put);
		List<Map<String, Object>> serializedEnchs = new ArrayList<>();
		for (Entry<Enchantment, Integer> entry : enchs.entrySet()) {
			Map<String, Object> serializedEnch = new HashMap<>();
			serializedEnch.put(ENCHANTMENT_LEVEL_TAG, entry.getValue());
			serializedEnch.put(ENCHANTMENT_TYPE_TAG, XEnchantment.matchXEnchantment(entry.getKey()).name());
			serializedEnchs.add(serializedEnch);
		}
		return serializedEnchs;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.hasItemMeta();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.containsKey(ENCHANTMENTS_TAG)) {
			List<Map<String, Object>> enchs = (List<Map<String, Object>>) map.get(ENCHANTMENTS_TAG);
			for (Map<String, Object> ench : enchs) {
				item.addUnsafeEnchantment(
						XEnchantment.matchXEnchantment((String) ench.get(ENCHANTMENT_TYPE_TAG)).get().getEnchant(),
						(int) ench.get(ENCHANTMENT_LEVEL_TAG));
			}
		}
		if (map.containsKey(DISPLAY_TAG)) {
			Map<String, Object> serMeta = (Map<String, Object>) map.get(DISPLAY_TAG);
			ItemMeta meta = item.getItemMeta();
			if (serMeta.containsKey(LORE_TAG))
				meta.setLore(((List<String>) serMeta.get(LORE_TAG)).stream()
						.map(s -> ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE, s))
						.collect(Collectors.toList()));
			if (serMeta.containsKey(DISPLAYNAME_TAG))
				meta.setDisplayName(ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE,
						(String) serMeta.get(DISPLAYNAME_TAG)));
			if (serMeta.containsKey(LOCALIZED_NAME_TAG))
				meta.setLocalizedName(ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE,
						(String) serMeta.get(LOCALIZED_NAME_TAG)));
			meta.setCustomModelData((Integer) serMeta.get(CUSTOM_MODEL_DATA));
			item.setItemMeta(meta);
		}
		if (map.containsKey(ATTRIBUTES_TAG))
			deserializeAttributeModifiers(item, map);
	}

	@SuppressWarnings("unchecked")
	private void deserializeAttributeModifiers(ItemStack item, Map<String, Object> map) {
		ItemMeta meta = item.getItemMeta();
		for (Map<String, Object> attribute : (List<Map<String, Object>>) map.get(ATTRIBUTES_TAG)) {
			meta.addAttributeModifier(Attribute.valueOf((String) attribute.get(ATTRIBUTE_TYPE_TAG)),
					new AttributeModifier(UUID.fromString((String) attribute.get(ATTRIBUTE_UUID_TAG)),
							(String) attribute.get(ATTRIBUTE_NAME_TAG), (double) attribute.get(ATTRIBUTE_AMOUNT_TAG),
							Operation.valueOf((String) attribute.get(ATTRIBUTE_OPERATION_TAG)),
							attribute.containsKey(ATTRIBUTE_SLOT_TAG)
									? EquipmentSlot.valueOf((String) attribute.get(ATTRIBUTE_SLOT_TAG))
									: null));
		}
		item.setItemMeta(meta);
	}

	@Override
	public String getKey() {
		return UNSPECIFIC_META_TAG;
	}
}
