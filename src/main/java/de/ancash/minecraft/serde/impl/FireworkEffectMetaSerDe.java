package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import de.ancash.minecraft.serde.ItemDeserializer;
import de.ancash.minecraft.serde.ItemSerializer;

public class FireworkEffectMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final FireworkEffectMetaSerDe INSTANCE = new FireworkEffectMetaSerDe();

	FireworkEffectMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
		if (meta.hasEffect()) {
			map = ItemSerializer.INSTANCE.serialize(meta.getEffect());
			meta.setEffect(null);
		}
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof FireworkEffectMeta;
	}

	@Override
	public String getKey() {
		return FIREWORK_EFFECT_TAG;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.isEmpty())
			return;
		FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
		meta.setEffect(ItemDeserializer.INSTANCE.deserializeFireworkEffect(map));
		item.setItemMeta(meta);
	}
}
