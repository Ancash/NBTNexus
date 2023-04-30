package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.SPAWN_EGG_TAG;
import static de.ancash.minecraft.serde.IItemTags.SPAWN_EGG_TYPE_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class SpawnEggMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final SpawnEggMetaSerDe INSTANCE = new SpawnEggMetaSerDe();

	SpawnEggMetaSerDe() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
		EntityType type = meta.getSpawnedType();
		if (type != null) {
			map.put(SPAWN_EGG_TYPE_TAG, type.name());
		}
		meta.setSpawnedType(null);
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof SpawnEggMeta;
	}

	@Override
	public String getKey() {
		return SPAWN_EGG_TAG;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
		if (map.containsKey(SPAWN_EGG_TYPE_TAG))
			meta.setSpawnedType(EntityType.valueOf((String) map.get(SPAWN_EGG_TYPE_TAG)));
		else
			meta.setSpawnedType(null);
		item.setItemMeta(meta);
	}
}
