package de.ancash.nbtnexus.editor.validator;

import de.ancash.minecraft.inventory.editor.yml.IHandlerMapper;
import de.ancash.minecraft.inventory.editor.yml.gui.ConfigurationSectionEditor;
import de.ancash.minecraft.inventory.editor.yml.handler.ByteHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.DoubleHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.FloatHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.IValueHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.IntegerHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.ListHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.LongHandler;
import de.ancash.minecraft.inventory.editor.yml.handler.ShortHandler;
import de.ancash.nbtnexus.MetaTag;
import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.NBTTag;

public class HandlerMapper implements IHandlerMapper {

	public IValueHandler<?> getHandler(ConfigurationSectionEditor cur, String key) {
		if (ValidatorUtil.isItemRoot(cur) && MetaTag.AMOUNT_TAG.equals(key))
			return ByteHandler.INSTANCE;

		IValueHandler<?> temp = IHandlerMapper.super.getHandler(cur, key);
		String[] split = key.split(NBTNexus.SPLITTER_REGEX);
		if (split.length == 1 || temp instanceof ListHandler)
			return temp;
		NBTTag type = NBTTag.valueOf(split[split.length - 1]);
		switch (type) {
		case BYTE:
			return ByteHandler.INSTANCE;
		case SHORT:
			return ShortHandler.INSTANCE;
		case INT:
			return IntegerHandler.INSTANCE;
		case LONG:
			return LongHandler.INSTANCE;
		case FLOAT:
			return FloatHandler.INSTANCE;
		case DOUBLE:
			return DoubleHandler.INSTANCE;
		default:
			return temp;
		}
	}
}
