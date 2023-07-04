package de.ancash.nbtnexus.serde.access;

import java.util.Map;

import de.ancash.nbtnexus.serde.SerializedItem;

public abstract class SerializedMetaAccess {

	public static final UnspecificMetaAccess UNSPECIFIC_META_ACCESS = new UnspecificMetaAccess();

	protected final String key;

	protected SerializedMetaAccess(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public boolean isValid(SerializedItem si) {
		return si.getMap().containsKey(key);
	}

	public boolean isValid(Map<String, Object> map) {
		return map.containsKey(key);
	}
}
