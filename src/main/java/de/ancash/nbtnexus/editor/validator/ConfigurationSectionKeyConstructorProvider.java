package de.ancash.nbtnexus.editor.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.ancash.minecraft.inventory.editor.yml.ConfigurationSectionKeyConstructor;
import de.ancash.minecraft.inventory.editor.yml.IConfigurationSectionKeyConstructorProvider;
import de.ancash.minecraft.inventory.editor.yml.gui.ConfigurationSectionEditor;
import de.ancash.minecraft.inventory.editor.yml.handler.ConfigurationSectionHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.ListHandler;
import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.serde.structure.SerDeStructure;

public class ConfigurationSectionKeyConstructorProvider implements IConfigurationSectionKeyConstructorProvider {

	private final SerDeStructure structure = NBTNexus.getInstance().getStructure();

	@SuppressWarnings("nls")
	@Override
	public Set<ConfigurationSectionKeyConstructor> getKeyConstructor(ConfigurationSectionEditor cur) {
		ConfigurationSectionEditor root = ValidatorUtil.getItemRoot(cur);
		if (root == null || !ValidatorUtil.isItemRoot(root))
			return null;
		String path = ValidatorUtil.getPath(root, cur);
		System.out.println("path: " + path + ": " + path.isEmpty());
		System.out.println("exists: " + structure.containsKey(path) + ": " + structure.get(path));

		SerDeStructure base = structure.getMap(path);
		if (path.isEmpty())
			base = structure;
		else if (!structure.containsKey(path))
			return null;
		else if (structure.isList(path) || structure.isMap(path))
			base = (SerDeStructure) structure.get(path);
		Set<ConfigurationSectionKeyConstructor> suggestions = new HashSet<>();
		for (String sug : base.getKeys(false)) {
			if (base.isMap(sug))
				suggestions.add(new ConfigurationSectionKeyConstructor(sug, ConfigurationSectionHandler.INSTANCE, null,
						base.getMap(sug).toString()));
			else if (base.isList(sug)) {
				SerDeStructure list = base.getList(sug);
				suggestions.add(new ConfigurationSectionKeyConstructor(sug, ListHandler.INSTANCE,
						Arrays.asList(list.getListType().getHandler().defaultValue()), list.toString()));
			} else
				suggestions.add(new ConfigurationSectionKeyConstructor(sug, base.getEntry(sug).getType().getHandler()));
		}
		return suggestions;
	}

}
