package de.ancash.nbtnexus.editor.validator;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.inventory.editor.yml.ValueEditor;
import de.ancash.minecraft.inventory.editor.yml.handler.BooleanHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.ConfigurationSectionHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.DoubleHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.IValueHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.ListHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.LongHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.StringHandler;
import de.ancash.minecraft.inventory.editor.yml.listener.IKeyValidator;
import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.NBTTag;

public class KeyValidator implements IKeyValidator {

	@SuppressWarnings("nls")
	@Override
	public Duplet<String, String> validate(ValueEditor<?> cur, IValueHandler<?> type, String key) {
		ValueEditor<?> ve = ValidatorUtil.getOneBeforeItemRoot(cur);

		if (ve != null) {
			if (ValidatorUtil.isMetaEditor(ve))
				return handleMetaKey(cur, type, key, ve);
			ve = ve.getParent();
		} else
			ve = cur;

		if (!ValidatorUtil.isItemRoot(ve))
			return Tuple.of(key, null);

		String ending = "";
		if (type instanceof DoubleHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.DOUBLE.name();
		} else if (type instanceof LongHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.LONG.name();
		} else if (type instanceof StringHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.STRING.name();
		} else if (type instanceof ConfigurationSectionHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.COMPOUND.name();
		} else if (type instanceof BooleanHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.BOOLEAN.name();
		} else if (type instanceof ListHandler) {
			ending = NBTNexus.SPLITTER + NBTTag.LIST.name() + NBTNexus.SPLITTER + NBTTag.DYNAMIC;
		}

		if (!key.endsWith(ending))
			return Tuple.of(key.split(NBTNexus.SPLITTER_REGEX)[0] + ending, null);
		return Tuple.of(key, null);
	}

	protected Duplet<String, String> handleMetaKey(ValueEditor<?> arg0, IValueHandler<?> arg1, String arg2,
			ValueEditor<?> meta) {
		return Tuple.of(arg2, null);
	}
}
