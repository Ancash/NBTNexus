package de.ancash.nbtnexus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.ancash.minecraft.nbt.NBTType;

public enum NBTTag {

	BYTE(NBTType.NBTTagByte, Byte.class),
	COMPOUND(NBTType.NBTTagCompound, Map.class, HashMap.class, LinkedHashMap.class),
	DOUBLE(NBTType.NBTTagDouble, Double.class), FLOAT(NBTType.NBTTagFloat, Float.class),
	INT(NBTType.NBTTagInt, Integer.class), STRING(NBTType.NBTTagString, String.class),
	SHORT(NBTType.NBTTagShort, Short.class), LONG(NBTType.NBTTagLong, Long.class),
	BYTE_ARRAY(NBTType.NBTTagByteArray, Byte[].class, byte[].class),
	INT_ARRAY(NBTType.NBTTagIntArray, Integer[].class, int[].class),
	LIST(NBTType.NBTTagList, List.class, ArrayList.class, LinkedList.class), ITEM_STACK(null), ITEM_STACK_ARRAY(null),
	ITEM_STACK_LIST(null), UUID(null), BOOLEAN(null, Boolean.class), END(NBTType.NBTTagEnd), OBJECT(null);

	private static final Map<NBTType, NBTTag> byType = new HashMap<>();
	private static final Map<Class<?>, NBTTag> byClazz = new HashMap<>();

	static {
		for (NBTTag val : values()) {
			if (val.type != null)
				byType.put(val.type, val);
			if (val.clazz != null && !val.clazz.isEmpty())
				val.clazz.forEach(c -> byClazz.put(c, val));
		}
	}

	public static NBTTag getByNBTType(NBTType type) {
		return byType.get(type);
	}

	public static NBTTag getByClazz(Class<?> c) {
		return byClazz.get(c);
	}

	private final de.ancash.minecraft.nbt.NBTType type;
	private final List<Class<?>> clazz;

	private NBTTag(de.ancash.minecraft.nbt.NBTType type, Class<?>... clazz) {
		this.type = type;
		this.clazz = Collections.unmodifiableList(Arrays.asList(clazz));
	}

	public List<Class<?>> getClazz() {
		return clazz;
	}

	public de.ancash.minecraft.nbt.NBTType getType() {
		return type;
	}

	public enum Special {

	}
}
