package de.ancash.nbtnexus.serde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.bukkit.inventory.ItemStack;

public class SerializedItem {

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

	public String toYaml() throws IOException {
		return Serializer.toYaml(map);
	}

	public boolean isSimilar(SerializedItem item) {
		return isSimilar(item.map, null);
	}

	public boolean isSimilar(ItemStack item) {
		return isSimilar(of(item), null);
	}

	public boolean isSimilar(ItemStack item, Set<String> ignore) {
		return isSimilar(of(item), ignore);
	}

	public boolean isSimilar(SerializedItem item, Set<String> ignore) {
		return isSimilar(item.map);
	}

	public boolean isSimilar(Map<String, Object> item) {
		return isSimilar(item, null);
	}

	public boolean isSimilar(Map<String, Object> item, Set<String> ignore) {
		return compareMap(item, map, ignore, "");
	}

	@SuppressWarnings("nls")
	protected static boolean compareMap(Map<String, Object> a, Map<String, Object> b, Set<String> ignore, String path) {
		if (ignore == null)
			ignore = new HashSet<>();
		if (path == null)
			path = "";
		Set<String> keys = new HashSet<>();
		keys.addAll(a.keySet());
		keys.addAll(b.keySet());
		for (String key : keys) {
			String curp = null;
			if (path.isEmpty())
				curp = key;
			else
				curp = String.join(".", path, key);
			if (ignore.contains(curp))
				continue;
			if (a.containsKey(key) != b.containsKey(key))
				return false;
			if (!compareObjects(a.get(key), b.get(key), ignore, curp))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	protected static boolean compareObjects(Object oa, Object ob, Set<String> ignore, String path) {
		if ((oa != null) != (ob != null))
			return false;
		if (oa == null)
			return true;
		if (oa instanceof Map == ob instanceof Map && oa instanceof Map)
			return compareMap((Map<String, Object>) oa, (Map<String, Object>) ob, ignore, path);
		if (oa instanceof List == ob instanceof List && oa instanceof List)
			return compareList((List<Object>) oa, (List<Object>) ob, ignore, path);
		if (oa.getClass().isArray() && ob.getClass().isArray()) {
			Object ta = oa;
			Object tb = ob;
			if (oa instanceof byte[] && ob instanceof byte[]) {
				oa = IntStream.range(0, ((byte[]) oa).length).boxed().map(i -> ((byte[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((byte[]) ob).length).boxed().map(i -> ((byte[]) tb)[i]).toArray();
			} else if (oa instanceof short[] && ob instanceof short[]) {
				oa = IntStream.range(0, ((short[]) oa).length).boxed().map(i -> ((short[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((short[]) ob).length).boxed().map(i -> ((short[]) tb)[i]).toArray();
			} else if (oa instanceof int[] && ob instanceof int[]) {
				oa = IntStream.range(0, ((int[]) oa).length).boxed().map(i -> ((int[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((int[]) ob).length).boxed().map(i -> ((int[]) tb)[i]).toArray();
			} else if (oa instanceof long[] && ob instanceof long[]) {
				oa = IntStream.range(0, ((long[]) oa).length).boxed().map(i -> ((long[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((long[]) ob).length).boxed().map(i -> ((long[]) tb)[i]).toArray();
			} else if (oa instanceof float[] && ob instanceof float[]) {
				oa = IntStream.range(0, ((float[]) oa).length).boxed().map(i -> ((float[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((float[]) ob).length).boxed().map(i -> ((float[]) tb)[i]).toArray();
			} else if (oa instanceof double[] && ob instanceof double[]) {
				oa = IntStream.range(0, ((double[]) oa).length).boxed().map(i -> ((double[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((double[]) ob).length).boxed().map(i -> ((double[]) tb)[i]).toArray();
			} else if (oa instanceof char[] && ob instanceof char[]) {
				oa = IntStream.range(0, ((char[]) oa).length).boxed().map(i -> ((char[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((char[]) ob).length).boxed().map(i -> ((char[]) tb)[i]).toArray();
			} else if (oa instanceof boolean[] && ob instanceof boolean[]) {
				oa = IntStream.range(0, ((boolean[]) oa).length).boxed().map(i -> ((boolean[]) ta)[i]).toArray();
				ob = IntStream.range(0, ((boolean[]) ob).length).boxed().map(i -> ((boolean[]) tb)[i]).toArray();
			}
			return compareList(Arrays.asList((Object[]) oa), Arrays.asList((Object[]) ob), ignore, path);
		}
		return oa.equals(ob);
	}

	protected static boolean compareList(List<Object> a, List<Object> b, Set<String> ignore, String path) {
		if (a.size() != b.size())
			return false;
		a = new ArrayList<>(a);
		b = new ArrayList<>(b);

		Iterator<Object> itera = a.iterator();
		while (itera.hasNext()) {
			Object oa = itera.next();
			Iterator<Object> iterb = b.iterator();
			while (iterb.hasNext()) {
				Object ob = iterb.next();
				if (compareObjects(oa, ob, ignore, path)) {
					iterb.remove();
					break;
				}

			}
			if (a.size() == b.size())
				return false;
			itera.remove();
		}
		return true;
	}
}
