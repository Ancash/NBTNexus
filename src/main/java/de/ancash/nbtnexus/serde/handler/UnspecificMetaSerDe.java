package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.ancash.minecraft.cryptomorin.xseries.XEnchantment;
import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.structure.SerDeStructure;
import de.ancash.nbtnexus.serde.structure.SerDeStructureEntry;
import net.md_5.bungee.api.ChatColor;

public class UnspecificMetaSerDe implements IItemSerDe {

	public static final UnspecificMetaSerDe INSTANCE = new UnspecificMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(DISPLAYNAME_TAG, new SerDeStructureEntry<String>(NBTTag.STRING));
		structure.putList(LORE_TAG, NBTTag.STRING);
		structure.put(LOCALIZED_NAME_TAG, new SerDeStructureEntry<String>(NBTTag.STRING));
		structure.put(CUSTOM_MODEL_DATA, new SerDeStructureEntry<Integer>(NBTTag.INT));
		structure.putList(ENCHANTMENTS_TAG, NBTTag.COMPOUND);
		SerDeStructure enchs = structure.getList(ENCHANTMENTS_TAG);
		enchs.put(ENCHANTMENT_LEVEL_TAG, new SerDeStructureEntry<Integer>(NBTTag.INT));
		enchs.put(ENCHANTMENT_TYPE_TAG, SerDeStructureEntry.forEnum(XEnchantment.class));
		structure.putList(ATTRIBUTES_TAG, NBTTag.COMPOUND);
		SerDeStructure attr = structure.getList(ATTRIBUTES_TAG);
		attr.put(ATTRIBUTE_TYPE_TAG, SerDeStructureEntry.forEnum(Attribute.class));
		attr.put(ATTRIBUTE_NAME_TAG, new SerDeStructureEntry<String>(NBTTag.STRING));
		attr.put(ATTRIBUTE_AMOUNT_TAG, new SerDeStructureEntry<Double>(NBTTag.DOUBLE));
		attr.put(ATTRIBUTE_OPERATION_TAG, SerDeStructureEntry.forEnum(Operation.class));
		attr.put(ATTRIBUTE_UUID_TAG, SerDeStructureEntry.forUUID());
	}

	public SerDeStructure getStructure() {
		return (SerDeStructure) structure.clone();
	}

	UnspecificMetaSerDe() {

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
		if (meta.hasLore()) {
			map.put(LORE_TAG, meta.getLore().stream().map(this::translateChatColor).collect(Collectors.toList()));
			meta.setLore(null);
		}
		if (meta.hasDisplayName()) {
			map.put(DISPLAYNAME_TAG, translateChatColor(meta.getDisplayName()));
			meta.setDisplayName(null);
		}
		if (meta.hasLocalizedName()) {
			map.put(LOCALIZED_NAME_TAG, translateChatColor(meta.getLocalizedName()));
			meta.setLocalizedName(null);
		}
		if (meta.hasCustomModelData()) {
			map.put(CUSTOM_MODEL_DATA, meta.getCustomModelData());
			meta.setCustomModelData(null);
		}

		item.setItemMeta(meta);
		if (!item.getEnchantments().isEmpty()) {
			map.put(ENCHANTMENTS_TAG, serializeEnchantments(item));
			item.getEnchantments().keySet().forEach(item::removeEnchantment);
		}
		meta = item.getItemMeta();

		if (!meta.getItemFlags().isEmpty()) {
			map.put(ITEM_FLAGS_TAG, meta.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList()));
			meta.getItemFlags().forEach(meta::removeItemFlags);
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

		ItemMeta meta = item.getItemMeta();
		if (map.containsKey(LORE_TAG))
			meta.setLore(((List<?>) map.get(LORE_TAG)).stream().map(String::valueOf)
					.map(s -> ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE, s))
					.collect(Collectors.toList()));
		if (map.containsKey(DISPLAYNAME_TAG))
			meta.setDisplayName(ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE,
					String.valueOf(map.get(DISPLAYNAME_TAG))));
		if (map.containsKey(LOCALIZED_NAME_TAG))
			meta.setLocalizedName(ChatColor.translateAlternateColorCodes(ALTERNATE_COLOR_CODE,
					String.valueOf(map.get(LOCALIZED_NAME_TAG))));
		if (map.containsKey(CUSTOM_MODEL_DATA))
			meta.setCustomModelData(Integer.valueOf(String.valueOf(map.get(CUSTOM_MODEL_DATA))));

		if (map.containsKey(ITEM_FLAGS_TAG))
			((List<String>) map.get(ITEM_FLAGS_TAG)).stream().map(ItemFlag::valueOf).forEach(meta::addItemFlags);

		item.setItemMeta(meta);
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
