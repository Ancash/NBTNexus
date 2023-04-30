package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.BASE_POTION_EXTENDED_TAG;
import static de.ancash.minecraft.serde.IItemTags.BASE_POTION_TAG;
import static de.ancash.minecraft.serde.IItemTags.BASE_POTION_TYPE_TAG;
import static de.ancash.minecraft.serde.IItemTags.BASE_POTION_UPGRADED_TAG;
import static de.ancash.minecraft.serde.IItemTags.BLUE_TAG;
import static de.ancash.minecraft.serde.IItemTags.GREEN_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_COLOR_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECTS_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_TAG;
import static de.ancash.minecraft.serde.IItemTags.RED_TAG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import de.ancash.minecraft.serde.ItemDeserializer;
import de.ancash.minecraft.serde.ItemSerializer;

public class PotionMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final PotionMetaSerDe INSTANCE = new PotionMetaSerDe();

	PotionMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		map.put(POTION_EFFECTS_TAG,
				meta.getCustomEffects().stream().map(ItemSerializer.INSTANCE::serialize).collect(Collectors.toList()));

		PotionData potionData = meta.getBasePotionData();
		Map<String, Object> basePotion = new HashMap<>();
		basePotion.put(BASE_POTION_TYPE_TAG, potionData.getType().name());
		basePotion.put(BASE_POTION_EXTENDED_TAG, potionData.isExtended());
		basePotion.put(BASE_POTION_UPGRADED_TAG, potionData.isUpgraded());
		map.put(BASE_POTION_TAG, basePotion);

		Map<String, Object> color = new HashMap<>();
		color.put(RED_TAG, meta.getColor().getRed());
		color.put(GREEN_TAG, meta.getColor().getGreen());
		color.put(BLUE_TAG, meta.getColor().getBlue());
		map.put(POTION_COLOR_TAG, color);

		meta.clearCustomEffects();
		item.setItemMeta(meta);
		item.setType(Material.BEDROCK);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof PotionMeta;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		((List<Map<String, Object>>) map.get(POTION_EFFECTS_TAG)).stream()
				.map(ItemDeserializer.INSTANCE::deserializePotionEffect).forEach(e -> meta.addCustomEffect(e, true));
		Map<String, Object> potionBase = (Map<String, Object>) map.get(BASE_POTION_TAG);
		meta.setBasePotionData(new PotionData(PotionType.valueOf((String) potionBase.get(BASE_POTION_TYPE_TAG)),
				(boolean) potionBase.get(BASE_POTION_EXTENDED_TAG),
				(boolean) potionBase.get(BASE_POTION_UPGRADED_TAG)));

		Map<String, Object> color = (Map<String, Object>) map.get(POTION_COLOR_TAG);

		meta.setColor(Color.fromRGB((int) color.remove(RED_TAG), (int) color.remove(GREEN_TAG),
				(int) color.remove(BLUE_TAG)));

		item.setItemMeta(meta);
	}

	@Override
	public String getKey() {
		return POTION_TAG;
	}
}
