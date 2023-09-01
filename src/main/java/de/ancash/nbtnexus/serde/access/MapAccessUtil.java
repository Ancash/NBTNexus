package de.ancash.nbtnexus.serde.access;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class MapAccessUtil {

	@SuppressWarnings({ "unchecked", "nls" })
	public static boolean exists(Map<String, Object> map, String key) {
		String[] split = key.split("\\.");
		for (int i = 0; i < split.length; i++) {
			if (!map.containsKey(split[i]))
				return false;
			Object o = map.get(split[i]);
			if (!(o instanceof Map))
				return i == split.length - 1;
			map = (Map<String, Object>) o;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMap(Map<String, Object> map, String key) {
		return (Map<String, Object>) get(map, key);
	}

	@SuppressWarnings({ "nls", "unchecked" })
	public static Object get(Map<String, Object> map, String s) {
		String[] split = s.split("\\.");
		if (split.length == 1)
			return map.get(split[0]);

		if (!map.containsKey(split[0]))
			return null;

		Object val = map.get(split[0]);
		if (val instanceof Map)
			return get((Map<String, Object>) val, String.join(".", Arrays.copyOfRange(split, 1, split.length)));
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getList(Map<String, Object> map, String s) {
		return (List<T>) get(map, s);
	}

	public static int getInt(Map<String, Object> map, String s) {
		return (int) get(map, s);
	}

	public static String getString(Map<String, Object> map, String s) {
		return (String) get(map, s);
	}

	public static long getLong(Map<String, Object> map, String s) {
		return (long) get(map, s);
	}

}
