package de.ancash.minecraft.serde.editor;

import static de.ancash.minecraft.serde.IItemTags.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DisplayEditor implements IEditor {

	private final Map<String, Object> display;

	public DisplayEditor(Map<String, Object> display) {
		this.display = display;
	}

	public DisplayEditor setDisplayName(String string) {
		if(string == null)
			display.remove(DISPLAYNAME_TAG);
		else
			display.put(DISPLAYNAME_TAG, string);
		return this;
	}

	public String getDisplayName() {
		return (String) display.get(DISPLAYNAME_TAG);
	}
	
	public DisplayEditor setLocalizedName(String string) {
		if(string == null)
			display.remove(LOCALIZED_NAME_TAG);
		else
			display.put(LOCALIZED_NAME_TAG, string);
		return this;
	}

	public String getLocalizedName() {
		return (String) display.get(LOCALIZED_NAME_TAG);
	}

	public DisplayEditor setLore(List<String> lore) {
		if(lore == null || lore.isEmpty())
			display.remove(LORE_TAG);
		else
			display.put(LORE_TAG, lore);
		return this;
	}
	
	public DisplayEditor setLore(String...lore) {
		return setLore(Arrays.asList(lore));
	}
	
	@Override
	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(display);
	}

	@Override
	public String getTag() {
		return DISPLAY_TAG;
	}
}
