package de.ancash.nbtnexus.serde;

import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

public interface IItemSerializer {

	public Map<String, Object> serialize(ItemStack item);

	public boolean isValid(ItemStack item);

	public String getKey();

	public default Set<String> getBlacklistedKeys() {
		return null;
	}

	public default boolean hasBlacklistedKeys() {
		return getBlacklistedKeys() != null && !getBlacklistedKeys().isEmpty();
	}

	public default boolean hasKeysToRelocate() {
		return getKeysToRelocate() != null && !getKeysToRelocate().isEmpty();
	}

	public default Map<String, String> getKeysToRelocate() {
		return null;
	}
}
