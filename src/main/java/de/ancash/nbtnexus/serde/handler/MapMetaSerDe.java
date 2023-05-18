package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerDeStructure;

@Deprecated
public class MapMetaSerDe implements IItemSerDe {

	public static final MapMetaSerDe INSTANCE = new MapMetaSerDe();

	@SuppressWarnings("nls")
	private static final Set<String> bl = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("map" + NBTNexus.SPLITTER + NBTTag.INT)));

	MapMetaSerDe() {
	}

	@Override
	public Set<String> getBlacklistedKeys() {
		return bl;
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		MapMeta meta = (MapMeta) item.getItemMeta();
		Map<String, Object> map = new HashMap<>();
		if (meta.hasColor())
			map.put(MAP_COLOR_TAG, ItemSerializer.INSTANCE.serializeColor(meta.getColor()));
		meta.setColor(null);
		if (meta.hasMapId())
			map.put(MAP_ID_TAG, meta.getMapId());
		if (meta.isScaling())
			map.put(MAP_SCALING_TAG, meta.isScaling());
		if (meta.hasMapView()) {
			map.put(MAP_VIEW_TAG, ItemSerializer.INSTANCE.serializeMapView(meta.getMapView()));
		}
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof MapMeta;
	}

	@Override
	public String getKey() {
		return MAP_TAG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		MapMeta meta = (MapMeta) item.getItemMeta();
		if (map.containsKey(MAP_COLOR_TAG))
			meta.setColor(ItemDeserializer.INSTANCE.deserializeColor((Map<String, Object>) map.get(MAP_COLOR_TAG)));
		if (map.containsKey(MAP_VIEW_TAG))
			meta.setMapView(ItemDeserializer.INSTANCE.deserializeMapView((Map<String, Object>) map.get(MAP_VIEW_TAG)));
		if (map.containsKey(MAP_ID_TAG))
			meta.setMapId((int) map.get(MAP_ID_TAG));
		if (map.containsKey(MAP_SCALING_TAG))
			meta.setScaling((boolean) map.get(MAP_SCALING_TAG));
		item.setItemMeta(meta);
	}

	@Override
	public SerDeStructure getStructure() {
		return null;
	}
}
