package de.ancash.nbtnexus.serde.access;

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
		return (Map<String, Object>) map.get(key);
	}

	@SuppressWarnings("nls")
	public static Object get(Map<String, Object> map, String s) {
		String[] split = s.split("\\.");
		if (split.length == 1)
			return map.get(split[0]);
		Map<String, Object> cur = map;
		for (int i = 0; i < split.length; i++) {
			if (i + 1 == split.length)
				break;
			if (cur.containsKey(split[i]) || !(cur.get(split[i]) instanceof Map))
				return null;
			cur = getMap(map, split[i]);
		}
		return cur.get(split[split.length - 1]);
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
