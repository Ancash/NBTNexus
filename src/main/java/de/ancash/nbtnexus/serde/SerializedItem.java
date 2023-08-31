package de.ancash.nbtnexus.serde;

import static de.ancash.nbtnexus.MetaTag.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;

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
		return of(map, true);
	}

	public static SerializedItem of(Map<String, Object> map, boolean immutable) {
		return new SerializedItem(map, immutable);
	}
	
//	public static void main(String[] args) {
//		System.out.println(
//				Array.newInstance(new Object[1].getClass().getComponentType(), 1).getClass().getComponentType());
//		System.out.println(
//				Array.newInstance(new Object[1][].getClass().getComponentType(), 1).getClass().getComponentType());
//		System.out.println(
//				Array.newInstance(new Object[1][][].getClass().getComponentType(), 1).getClass().getComponentType());
//		Object[][] test = new Object[1][1];
//		test[0] = new Object[2];
//		test[0][0] = 12;
//		test[0][1] = "asdasd";
//		Object[][] clone = (Object[][]) deepCopyArray(test, Function.identity(), Function.identity());
//
//		for (int a = 0; a < test.length; a++) {
//			if (test[a] == null) {
//				Validate.isTrue(clone[a] == null, test[a] + ":" + clone[a]);
//				continue;
//			}
//			for (int b = 0; b < test[a].length; b++) {
//				if (test[a][b] == null) {
//					System.out.println(b + ": " + test[a][b] + "<=>" + clone[a][b]);
//					System.out.println(test[a][b] + "<=>" + clone[a][b]);
//					Validate.isTrue(clone[a][b] == null, test[a][b] + ":" + clone[a][b]);
//					continue;
//				}
//				Validate.isTrue(clone[a][b].equals(test[a][b]));
//			}
//		}
//		System.out.println("deep clone");
//	}

	@SuppressWarnings({ "unchecked", "nls" })
	private static List<String> getKeyPaths(Map<String, Object> m, String curPath) {
		List<String> paths = new ArrayList<>();
		for (Entry<String, Object> entry : m.entrySet()) {
			String path = String.join(".", curPath, entry.getKey());
			if (path.startsWith("."))
				path.replaceFirst("\\.", "");

			if (!(entry.getValue() instanceof Map))
				paths.add(path);
			else
				paths.addAll(getKeyPaths((Map<String, Object>) entry.getValue(), path));
		}
		return paths;
	}

	private final HashMap<String, Object> map;
	private final boolean immutable;
	private final int keyHash;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	SerializedItem(Map<String, Object> map, boolean immutable) {
		this.immutable = immutable;

		if (immutable) {
			this.map = CloneUtil.deepCopy(map, m -> (HashMap<String, Object>) Collections.unmodifiableMap(m),
					l -> (ArrayList) Collections.unmodifiableList(l));
			keyHash = keyHashCode0();
		} else {
			this.map = CloneUtil.deepCopy(map, Function.identity(), Function.identity());
			keyHash = 0;
		}

	}

	public boolean isImmutable() {
		return immutable;
	}

	@SuppressWarnings("nls")
	private int keyHashCode0() {
		return getKeyPaths(map, "").hashCode();
	}

	public int keyHashCode() {
		if (immutable)
			return keyHash;
		return keyHashCode0();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(!(obj instanceof SerializedItem))
			return false;
		return areEqualIgnoreAmount((SerializedItem) obj);
	}
	
	public Set<String> getKeys() {
		return Collections.unmodifiableSet(map.keySet());
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
		return item.keyHashCode() == keyHashCode() && DefaultSerializedItemComparator.INSTANCE.areEqualIgnoreOrder(this, item, ignoreOrder);
	}

	public boolean areEqualIgnoreAmount(SerializedItem item) {
		return item.keyHashCode() == keyHashCode() && DefaultSerializedItemComparator.INSTANCE.areEqual(this, item, ignoreKey, ignoreOrder);
	}
}
