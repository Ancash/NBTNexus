package de.ancash.minecraft.serde.editor;

import java.util.Map;

public interface IEditor {

	public Map<String, Object> asMap();

	public String getTag();
}
