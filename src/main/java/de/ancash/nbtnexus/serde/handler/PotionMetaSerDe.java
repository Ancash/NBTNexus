package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerDeStructure;

public class PotionMetaSerDe implements IItemSerDe {

	public static final PotionMetaSerDe INSTANCE = new PotionMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(BASE_POTION_TAG, NBTTag.COMPOUND);
		structure.put(POTION_EFFECTS_TAG, NBTTag.LIST);
		structure.put(POTION_COLOR_TAG, NBTTag.COMPOUND);
	}

	public SerDeStructure getStructure() {
		return structure.clone();
	}

	PotionMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		map.put(POTION_EFFECTS_TAG, meta.getCustomEffects().stream().map(ItemSerializer.INSTANCE::serializePotionEffect)
				.collect(Collectors.toList()));

		PotionData potionData = meta.getBasePotionData();
		Map<String, Object> basePotion = new HashMap<>();
		basePotion.put(BASE_POTION_TYPE_TAG, potionData.getType().name());
		basePotion.put(BASE_POTION_EXTENDED_TAG, potionData.isExtended());
		basePotion.put(BASE_POTION_UPGRADED_TAG, potionData.isUpgraded());
		map.put(BASE_POTION_TAG, basePotion);

		if (meta.hasColor()) {
			map.put(POTION_COLOR_TAG, ItemSerializer.INSTANCE.serializeColor(meta.getColor()));
			meta.setColor(null);
		}
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

		if (map.containsKey(POTION_COLOR_TAG))
			meta.setColor(ItemDeserializer.INSTANCE.deserializeColor((Map<String, Object>) map.get(POTION_COLOR_TAG)));

		item.setItemMeta(meta);
	}

	@Override
	public String getKey() {
		return POTION_TAG;
	}
}
