package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.FIREWORK_EFFECTS_TAG;
import static de.ancash.nbtnexus.Tags.FIREWORK_POWER_TAG;
import static de.ancash.nbtnexus.Tags.FIREWORK_TAG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

public class FireworkMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final FireworkMetaSerDe INSTANCE = new FireworkMetaSerDe();

	FireworkMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		map.put(FIREWORK_POWER_TAG, meta.getPower());
		if (meta.hasEffects())
			map.put(FIREWORK_EFFECTS_TAG, meta.getEffects().stream()
					.map(ItemSerializer.INSTANCE::serializeFireworkEffect).collect(Collectors.toList()));
		meta.clearEffects();
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof FireworkMeta;
	}

	@Override
	public String getKey() {
		return FIREWORK_TAG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		meta.setPower((int) map.get(FIREWORK_POWER_TAG));
		if (map.containsKey(FIREWORK_EFFECTS_TAG)) {
			meta.addEffects(((List<Map<String, Object>>) map.get(FIREWORK_EFFECTS_TAG)).stream()
					.map(ItemDeserializer.INSTANCE::deserializeFireworkEffect).collect(Collectors.toList()));
		}
		item.setItemMeta(meta);
	}
}
