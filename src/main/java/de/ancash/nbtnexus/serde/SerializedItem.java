package de.ancash.nbtnexus.serde;

import static de.ancash.nbtnexus.MetaTag.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.MetaTag;
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

	public XMaterial getXMaterial() {
		return XMaterial.valueOf((String) map.get(MetaTag.XMATERIAL_TAG));
	}

	public boolean isMap(String key) {
		return map.containsKey(key) && map.get(key) instanceof Map;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap(String key) {
		return (Map<String, Object>) map.get(key);
	}

	@SuppressWarnings("nls")
	public Object get(String s) {
		String[] split = s.split("\\.");
		if (split.length == 1)
			return map.get(split[0]);
		Map<String, Object> cur = map;
		for (int i = 0; i < split.length; i++) {
			if (i + 1 == split.length)
				break;
			if (cur.containsKey(split[i]) || !(cur.get(split[i]) instanceof Map))
				return null;
			cur = getMap(split[i]);
		}
		return cur.get(split[split.length - 1]);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String s) {
		return (List<T>) get(s);
	}

	public int getInt(String s) {
		return (int) get(s);
	}

	public String getString(String s) {
		return (String) get(s);
	}

	public long getLong(String s) {
		return (long) get(s);
	}

	public int getAmount() {
		return (int) map.get(MetaTag.AMOUNT_TAG);
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public ItemStack toItem() {
		return ItemDeserializer.INSTANCE.deserializeItemStack(map);
	}

	public String toJson() throws IOException {
		return Serializer.toJson(map);
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
