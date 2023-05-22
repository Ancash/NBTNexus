package de.ancash.nbtnexus.serde;

import static de.ancash.nbtnexus.MetaTag.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.serde.comparator.DefaultSerializedItemComparator;

public class SerializedItem {

	protected static final Set<String> ignoreKey = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(AMOUNT_TAG)));
	protected static final Set<String> ignoreOrder = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(String.join(NBTNexus.SPLITTER, UNSPECIFIC_META_TAG, ENCHANTMENTS_TAG),
					String.join(NBTNexus.SPLITTER, UNSPECIFIC_META_TAG, ITEM_FLAGS_TAG),
					String.join(NBTNexus.SPLITTER, UNSPECIFIC_META_TAG, ATTRIBUTES_TAG))));

	public static SerializedItem of(ItemStack item) {
		return of(ItemSerializer.INSTANCE.serializeItemStack(item));
	}

	public static SerializedItem of(Map<String, Object> map) {
		return new SerializedItem(map);
	}

	private final HashMap<String, Object> map;

	SerializedItem(Map<String, Object> map) {
		this.map = new HashMap<>(map);
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public String toYaml() throws IOException {
		return Serializer.toYaml(map);
	}

	public boolean areEqual(SerializedItem item) {
		return DefaultSerializedItemComparator.INSTANCE.areEqualIgnoreOrder(this, item, ignoreOrder);
	}

	public boolean areEqualIgnoreAmount(SerializedItem item) {
		return DefaultSerializedItemComparator.INSTANCE.areEqual(this, item, ignoreKey, ignoreOrder);
	}
}
