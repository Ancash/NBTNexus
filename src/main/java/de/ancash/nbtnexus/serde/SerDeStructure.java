package de.ancash.nbtnexus.serde;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.ancash.libs.org.apache.commons.lang3.Validate;
import de.ancash.nbtnexus.NBTTag;

public class SerDeStructure implements Cloneable {

	private final HashMap<String, Object> map = new HashMap<>();

	public void putNewMap(String key) {
		map.put(key, new SerDeStructure());
	}

	public void put(String key, SerDeStructure s) {
		map.put(key, s);
	}

	public void put(String key, NBTTag type) {
		map.put(key, type);
	}

	@SuppressWarnings("nls")
	public Object get(String key) {
		Validate.notNull(key, "key null");
		String[] split = key.split("\\.");
		Object o = map.get(split[0]);
		if (split.length == 1)
			return o;
		if (!(o instanceof SerDeStructure))
			return null;
		return ((SerDeStructure) o).get(String.join(".", Arrays.copyOfRange(split, 1, split.length)));
	}

	@SuppressWarnings("nls")
	public boolean containsKey(String key) {
		Validate.notNull(key, "key null");
		String[] split = key.split("\\.");
		if (!map.containsKey(split[0]))
			return false;
		Object o = map.get(split[0]);
		if (split.length == 1)
			return true;
		if (!(o instanceof SerDeStructure))
			return false;
		return ((SerDeStructure) o).containsKey(String.join(".", Arrays.copyOfRange(split, 1, split.length)));
	}

	public boolean isMap(String key) {
		return containsKey(key) && get(key) instanceof SerDeStructure;
	}

	public boolean isNBTTag(String key) {
		return containsKey(key) && get(key) instanceof NBTTag;
	}

	public SerDeStructure getMap(String key) {
		return isMap(key) ? (SerDeStructure) get(key) : null;
	}

	@SuppressWarnings("nls")
	public Set<String> getKeys(boolean deep) {
		Set<String> keys = new HashSet<>();
		for (String k : map.keySet()) {
			keys.add(k);
			if (deep && isMap(k))
				getMap(k).getKeys(true).stream().map(s -> String.join(".", k, s)).forEach(keys::add);
		}
		return Collections.unmodifiableSet(keys);
	}

	public Object remove(String key) {
		return map.remove(key);
	}

	@Override
	public SerDeStructure clone() {
		try {
			return (SerDeStructure) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
