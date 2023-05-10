package de.ancash.nbtnexus.serde;

import java.io.IOException;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class SerializedItem {

	public static SerializedItem of(ItemStack item) {
		return of(ItemSerializer.INSTANCE.serializeItemStack(item));
	}

	public static SerializedItem of(Map<String, Object> map) {
		return new SerializedItem(map);
	}

	private final Map<String, Object> map;

	SerializedItem(Map<String, Object> map) {
		this.map = map;
	}

	public String toYaml() throws IOException {
		return Serializer.toYaml(map);
	}
}
