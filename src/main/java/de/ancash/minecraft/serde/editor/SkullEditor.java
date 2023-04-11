package de.ancash.minecraft.serde.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.ancash.minecraft.serde.IItemTags;

@SuppressWarnings("nls")
public class SkullEditor implements IEditor {

	public static final String SKULL_TAG = "SkullOwner" + IItemTags.SPLITTER + IItemTags.NBT_COMPOUND_TAG;
	public static final String SKULL_ID_TAG = "Id" + IItemTags.SPLITTER + IItemTags.UUID_TAG;
	public static final String SKULL_PROPERTIES_TAG = "Properties" + IItemTags.SPLITTER + IItemTags.NBT_COMPOUND_TAG;
	public static final String SKULL_TEXTURE_VALUE_TAG = "Value" + IItemTags.SPLITTER + IItemTags.NBT_STRING_TAG;
	public static final String SKULL_TEXTURES_TAG = "textures" + IItemTags.SPLITTER + IItemTags.NBT_LIST_TAG
			+ IItemTags.SPLITTER + IItemTags.NBT_COMPOUND_TAG;

	private final Map<String, Object> map;

	public SkullEditor(Map<String, Object> map) {
		this.map = map;
	}

	@Override
	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(map);
	}

	public SkullEditor setId(UUID id) {
		if (id == null) {
			map.remove(SKULL_ID_TAG);
			return this;
		}
		map.put(SKULL_ID_TAG, id.toString());
		return this;
	}

	public UUID getId() {
		if (!map.containsKey(SKULL_ID_TAG))
			return null;
		return UUID.fromString((String) map.get(SKULL_ID_TAG));
	}

	public SkullEditor setTexture(String texture) {
		return setTexture(texture, texture == null ? null : new UUID(texture.hashCode(), texture.hashCode()));
	}

	@SuppressWarnings("unchecked")
	public SkullEditor setTexture(String texture, UUID id) {
		setId(id);
		Map<String, Object> props = (Map<String, Object>) map.getOrDefault(SKULL_PROPERTIES_TAG, new HashMap<>());
		List<Map<String, Object>> textures = (List<Map<String, Object>>) props.getOrDefault(SKULL_TEXTURES_TAG,
				new ArrayList<>());
		Map<String, Object> value = new HashMap<>();
		value.put(SKULL_TEXTURE_VALUE_TAG, texture);
		textures.add(value);
		props.put(SKULL_TEXTURES_TAG, textures);
		map.put(SKULL_PROPERTIES_TAG, props);
		return this;
	}

	@Override
	public String getTag() {
		return SKULL_TAG; // $NON-NLS-1$
	}

}
