package de.ancash.nbtnexus.serde;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

public interface IItemDeserializer {

	public void deserialize(ItemStack item, Map<String, Object> map);

	public String getKey();

	public default boolean hasKeysToReverseRelocate() {
		return getKeysToReverseRelocate() != null && !getKeysToReverseRelocate().isEmpty();
	}

	public default Map<String, String> getKeysToReverseRelocate() {
		return null;
	}
}
