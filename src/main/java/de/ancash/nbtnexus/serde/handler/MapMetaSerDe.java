package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.MAP_COLOR_TAG;
import static de.ancash.nbtnexus.Tags.MAP_ID_TAG;
import static de.ancash.nbtnexus.Tags.MAP_SCALING_TAG;
import static de.ancash.nbtnexus.Tags.MAP_TAG;
import static de.ancash.nbtnexus.Tags.MAP_VIEW_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

@Deprecated
public class MapMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final MapMetaSerDe INSTANCE = new MapMetaSerDe();

	MapMetaSerDe() {
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
		meta.setScaling((boolean) map.get(MAP_SCALING_TAG));
		item.setItemMeta(meta);
	}
}
