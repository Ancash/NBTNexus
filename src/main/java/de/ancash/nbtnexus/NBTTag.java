package de.ancash.nbtnexus;

import java.util.HashMap;
import java.util.Map;

import de.ancash.minecraft.nbt.NBTType;

public enum NBTTag {

	BYTE(NBTType.NBTTagByte), COMPOUND(NBTType.NBTTagCompound), DOUBLE(NBTType.NBTTagDouble),
	FLOAT(NBTType.NBTTagFloat), INT(NBTType.NBTTagInt), STRING(NBTType.NBTTagString), SHORT(NBTType.NBTTagShort),
	LONG(NBTType.NBTTagLong), BYTE_ARRAY(NBTType.NBTTagByteArray), INT_ARRAY(NBTType.NBTTagIntArray),
	LIST(NBTType.NBTTagList), ITEM_STACK(null), ITEM_STACK_ARRAY(null), ITEM_STACK_LIST(null), UUID(null),
	END(NBTType.NBTTagEnd);

	private static final Map<NBTType, NBTTag> byType = new HashMap<>();

	static {
		for (NBTTag val : values())
			if (val.type != null)
				byType.put(val.type, val);
	}

	public static NBTTag getByNBTType(NBTType type) {
		return byType.get(type);
	}

	private final de.ancash.minecraft.nbt.NBTType type;

	private NBTTag(de.ancash.minecraft.nbt.NBTType type) {
		this.type = type;
	}

	public de.ancash.minecraft.nbt.NBTType getType() {
		return type;
	}
}
