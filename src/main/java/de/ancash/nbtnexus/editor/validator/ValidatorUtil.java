package de.ancash.nbtnexus.editor.validator;

import de.ancash.minecraft.inventory.editor.yml.ConfigurationSectionEditor;
import de.ancash.minecraft.inventory.editor.yml.ValueEditor;
import de.ancash.nbtnexus.MetaTag;

public class ValidatorUtil {

	public static boolean isItemProperty(ValueEditor<?> cur, int depth) {
		if (depth > 0) {
			if (!cur.hasParent())
				return false;
			return isItemProperty(cur.getParent(), depth - 1);
		}

		if (!(cur instanceof ConfigurationSectionEditor))
			return false;

		return ((ConfigurationSectionEditor) cur).getCurrent()
				.isConfigurationSection(MetaTag.NBT_NEXUS_ITEM_PROPERTIES_TAG);
	}

}
