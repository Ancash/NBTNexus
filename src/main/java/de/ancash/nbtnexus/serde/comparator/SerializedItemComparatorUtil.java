package de.ancash.nbtnexus.serde.comparator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import de.ancash.libs.org.apache.commons.lang3.Validate;
import de.ancash.nbtnexus.NBTNexusItem;

public class SerializedItemComparatorUtil {

	@SuppressWarnings("nls")
	public static boolean compareMap(Map<String, Object> a, Map<String, Object> b, Set<String> ignoredKeys,
			Set<String> ignoredOrder, String relativePath) {
		Validate.notNull(a);
		Validate.notNull(b);
		Validate.notNull(ignoredKeys);
		Validate.notNull(ignoredOrder);
		Validate.notNull(relativePath);
		Validate.isTrue(!ignoredKeys.contains(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG));

		if (a.containsKey(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG)
				|| b.containsKey(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG))
			relativePath = "";

		Set<String> keys = new HashSet<>();
		keys.addAll(a.keySet());
		keys.addAll(b.keySet());
		for (String key : keys) {
			String curp = null;
			if (relativePath.isEmpty())
				curp = key;
			else
				curp = String.join(".", relativePath, key);
			if (ignoredKeys.contains(curp))
				continue;
			if (a.containsKey(key) != b.containsKey(key))
				return false;
			if (!compareObjects(a.get(key), b.get(key), ignoredKeys, ignoredOrder, curp))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static boolean compareObjects(Object oa, Object ob, Set<String> ignoredKeys, Set<String> ignoredOrder,
			String relativePath) {
		if ((oa != null) != (ob != null))
			return false;
		if (oa == null)
			return true;
		Validate.notNull(oa);
		Validate.notNull(ob);
		Validate.notNull(ignoredKeys);
		Validate.notNull(ignoredOrder);
		Validate.notNull(relativePath);

		if (oa instanceof Map != ob instanceof Map)
			return false;

		if (oa instanceof List != ob instanceof List)
			return false;

		if (oa.getClass().isArray() != ob.getClass().isArray())
			return false;

		if (oa instanceof Map)
			return compareMap((Map<String, Object>) oa, (Map<String, Object>) ob, ignoredKeys, ignoredOrder,
					relativePath);

		if (oa instanceof List)
			return compareList((List<Object>) oa, (List<Object>) ob, ignoredKeys, ignoredOrder, relativePath);

		if (oa.getClass().isArray())
			return comparePrimitiveArrays(oa, ob, ignoredKeys, ignoredOrder, relativePath);

		return oa.equals(ob);
	}

	public static boolean comparePrimitiveArrays(Object oa, Object ob, Set<String> ignoredKeys,
			Set<String> ignoredOrder, String relativePath) {
		Validate.notNull(oa);
		Validate.notNull(ob);
		Validate.notNull(ignoredKeys);
		Validate.notNull(ignoredOrder);
		Validate.notNull(relativePath);
		return compareList(
				Arrays.asList(IntStream.range(0, Array.getLength(oa)).boxed().map(i -> Array.get(oa, i)).toArray()),
				Arrays.asList(IntStream.range(0, Array.getLength(ob)).boxed().map(i -> Array.get(ob, i)).toArray()),
				ignoredKeys, ignoredOrder, relativePath);
	}

	public static boolean compareList(List<Object> a, List<Object> b, Set<String> ignoredKeys, Set<String> ignoredOrder,
			String relativePath) {
		Validate.notNull(ignoredKeys);
		Validate.notNull(ignoredOrder);
		Validate.notNull(relativePath);

		if (a.size() != b.size())
			return false;

		boolean ignoreOrder = ignoredOrder.contains(relativePath);
		a = new ArrayList<>(a);
		b = new ArrayList<>(b);

		Iterator<Object> itera = a.iterator();
		while (itera.hasNext()) {
			Object oa = itera.next();
			Iterator<Object> iterb = b.iterator();
			while (iterb.hasNext()) {
				Object ob = iterb.next();
				if (compareObjects(oa, ob, ignoredKeys, ignoredOrder, relativePath)) {
					iterb.remove();
					break;
				}
				if (!ignoreOrder)
					return false;
			}
			if (a.size() == b.size())
				return false;
			itera.remove();
		}
		return true;
	}
}
