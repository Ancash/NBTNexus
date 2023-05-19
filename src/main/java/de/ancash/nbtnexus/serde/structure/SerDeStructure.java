package de.ancash.nbtnexus.serde.structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.ancash.libs.org.apache.commons.lang3.Validate;
import de.ancash.libs.org.apache.commons.text.WordUtils;
import de.ancash.nbtnexus.NBTTag;

public class SerDeStructure implements Cloneable {

	protected final HashMap<String, Object> map = new HashMap<>();
	protected final boolean isList;
	protected final NBTTag listType;

	public SerDeStructure() {
		this(false, null);
	}

	public SerDeStructure(boolean list, NBTTag listType) {
		this.isList = list;
		this.listType = listType;
		if (isList && (listType == null || listType.getHandler() == null))
			throw new IllegalArgumentException("list type null or no handler");
	}

	public void putMap(String key) {
		map.put(key, new SerDeStructure());
	}

	public void putList(String key, NBTTag type) {
		map.put(key, new SerDeStructure(true, type));
	}

	public void put(String key, SerDeStructure s) {
		map.put(key, s);
	}

	public void put(String key, SerDeStructureEntry<?> entry) {
		map.put(key, entry);
	}

	public NBTTag getListType() {
		return listType;
	}

	public boolean isList() {
		return isList;
	}

	@Override
	public String toString() {
		if (isList)
			return "List<" + WordUtils.capitalize(listType.name().toLowerCase()) + ">";
		return WordUtils.capitalize(NBTTag.COMPOUND.name().toLowerCase());
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
		return containsKey(key) && get(key) instanceof SerDeStructure && !((SerDeStructure) get(key)).isList;
	}

	public boolean isList(String key) {
		return containsKey(key) && get(key) instanceof SerDeStructure && ((SerDeStructure) get(key)).isList;
	}

	public SerDeStructure getList(String key) {
		return isList(key) ? (SerDeStructure) get(key) : null;
	}

	public boolean isEntry(String key) {
		return containsKey(key) && get(key) instanceof SerDeStructureEntry;
	}

	public SerDeStructure getMap(String key) {
		return isMap(key) ? (SerDeStructure) get(key) : null;
	}

	public SerDeStructureEntry<?> getEntry(String key) {
		return isEntry(key) ? (SerDeStructureEntry<?>) get(key) : null;
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
