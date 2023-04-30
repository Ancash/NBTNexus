package de.ancash.minecraft.serde;

import static de.ancash.minecraft.serde.IItemTags.DISPLAY_TAG;
import static de.ancash.minecraft.serde.IItemTags.XMATERIAL_TAG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cryptomorin.xseries.XMaterial;

import de.ancash.minecraft.serde.editor.DisplayEditor;
import de.ancash.minecraft.serde.editor.SkullEditor;

public class SerializedItem {

	private final Map<String, Object> map;
	private final DisplayEditor displayEditor;
	private final SkullEditor skullEditor;

	@SuppressWarnings("unchecked")
	SerializedItem(Map<String, Object> map) {
		this.map = map;
		displayEditor = new DisplayEditor((Map<String, Object>) map.computeIfAbsent(DISPLAY_TAG, k -> new HashMap<>()));
		if (map.get(XMATERIAL_TAG).equals(XMaterial.PLAYER_HEAD.name())) {
			skullEditor = new SkullEditor(
					(Map<String, Object>) map.computeIfAbsent(SkullEditor.SKULL_TAG, k -> new HashMap<>()));
		} else
			skullEditor = null;

	}

	public SkullEditor getSkullEditor() {
		return skullEditor;
	}

	public DisplayEditor getDisplayEditor() {
		return displayEditor;
	}

	public String toYaml() throws IOException {
		return Serializer.toYaml(map);
	}
}
