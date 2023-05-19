package de.ancash.nbtnexus.serde.structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.ancash.libs.org.apache.commons.lang3.Validate;
import de.ancash.nbtnexus.NBTTag;

public class SerDeStructureEntry<T> {

	public static final SerDeStructureEntry<Byte> BYTE = new SerDeStructureEntry<>(NBTTag.BYTE);
	public static final SerDeStructureEntry<Short> SHORT = new SerDeStructureEntry<>(NBTTag.SHORT);
	public static final SerDeStructureEntry<Integer> INT = new SerDeStructureEntry<>(NBTTag.INT);
	public static final SerDeStructureEntry<Long> LONG = new SerDeStructureEntry<>(NBTTag.LONG);
	public static final SerDeStructureEntry<Float> FLOAT = new SerDeStructureEntry<>(NBTTag.FLOAT);
	public static final SerDeStructureEntry<Double> DOUBLE = new SerDeStructureEntry<>(NBTTag.DOUBLE);
	public static final SerDeStructureEntry<String> STRING = new SerDeStructureEntry<>(NBTTag.STRING);
	public static final SerDeStructureEntry<Boolean> BOOLEAN = new SerDeStructureEntry<>(NBTTag.BOOLEAN);

	protected final NBTTag type;
	protected final Function<T, Boolean> validator;
	protected final List<String> suggestions;

	public SerDeStructureEntry(NBTTag type, String... suggestions) {
		this(type, o -> true, Arrays.asList(suggestions));
	}

	public SerDeStructureEntry(NBTTag type, Function<T, Boolean> validator, String... suggestions) {
		this(type, validator, Arrays.asList(suggestions));
	}

	public SerDeStructureEntry(NBTTag type, Function<T, Boolean> validator, List<String> suggestions) {
		Validate.notNull(type);
		Validate.notNull(validator);
		this.type = type;
		this.validator = validator;
		this.suggestions = Collections.unmodifiableList(suggestions);
	}

	public List<String> getSuggestions() {
		return suggestions;
	}

	@SuppressWarnings("unchecked")
	public boolean isValid(Object o) {
		return validator.apply((T) o);
	}

	public NBTTag getType() {
		return type;
	}

	public static <T extends Enum<T>> SerDeStructureEntry<String> forEnum(Class<T> clazz) {
		return new SerDeStructureEntry<String>(NBTTag.STRING, e -> {
			try {
				return Enum.valueOf(clazz, e) != null;
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}, Arrays.asList(clazz.getEnumConstants()).stream().map(Enum::name).collect(Collectors.toList()).toString()
				.replaceAll("(.{1,150})\\s+", "$1\n").split("\n"));
	}

	@SuppressWarnings("nls")
	public static String[] splitArray(Object[] o) {
		return Arrays.asList(o).stream().collect(Collectors.toList()).toString().replaceAll("(.{1,150})\\s+", "$1\n")
				.split("\n");
	}

	public static SerDeStructureEntry<String> forUUID(String... suggestions) {
		return new SerDeStructureEntry<String>(NBTTag.STRING, u -> {
			try {
				return UUID.fromString(u) != null;
			} catch (Exception e) {
				return false;
			}
		}, suggestions);
	}
}
