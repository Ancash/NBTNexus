package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.COMPASS_TAG;
import static de.ancash.nbtnexus.Tags.LODESTONE_LOCATION_TAG;
import static de.ancash.nbtnexus.Tags.LODESTONE_TRACKED_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import de.ancash.minecraft.nbt.utils.MinecraftVersion;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;

public class CompassMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final CompassMetaSerDe INSTANCE = new CompassMetaSerDe();

	CompassMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		CompassMeta meta = (CompassMeta) item.getItemMeta();
		if (meta.isLodestoneTracked())
			map.put(LODESTONE_TRACKED_TAG, meta.isLodestoneTracked());
		if (meta.hasLodestone())
			map.put(LODESTONE_LOCATION_TAG, meta.getLodestone().serialize());
		meta.setLodestone(null);
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)
				&& item.getItemMeta() instanceof CompassMeta;
	}

	@Override
	public String getKey() {
		return COMPASS_TAG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		CompassMeta meta = (CompassMeta) item.getItemMeta();
		if (map.containsKey(LODESTONE_TRACKED_TAG))
			meta.setLodestoneTracked((boolean) map.get(LODESTONE_TRACKED_TAG));
		if (map.containsKey(LODESTONE_LOCATION_TAG))
			meta.setLodestone(Location.deserialize((Map<String, Object>) map.get(LODESTONE_LOCATION_TAG)));
		item.setItemMeta(meta);
	}

}
