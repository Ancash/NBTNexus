package de.ancash.minecraft.serde;

import static de.ancash.minecraft.serde.IItemTags.AMOUNT_TAG;
import static de.ancash.minecraft.serde.IItemTags.BLUE_TAG;
import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_COLORS_TAG;
import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_FADE_COLORS_TAG;
import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_FLICKER_TAG;
import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_TRAIL_TAG;
import static de.ancash.minecraft.serde.IItemTags.FIREWORK_EFFECT_TYPE_TAG;
import static de.ancash.minecraft.serde.IItemTags.GREEN_TAG;
import static de.ancash.minecraft.serde.IItemTags.ITEM_STACK_ARRAY_TAG;
import static de.ancash.minecraft.serde.IItemTags.ITEM_STACK_LIST_TAG;
import static de.ancash.minecraft.serde.IItemTags.ITEM_STACK_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_AMBIENT_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_AMPLIFIER_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_DURATION_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_SHOW_ICON_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_SHOW_PARTICLES_TAG;
import static de.ancash.minecraft.serde.IItemTags.POTION_EFFECT_TYPE_TAG;
import static de.ancash.minecraft.serde.IItemTags.RED_TAG;
import static de.ancash.minecraft.serde.IItemTags.SPLITTER_REGEX;
import static de.ancash.minecraft.serde.IItemTags.UUID_TAG;
import static de.ancash.minecraft.serde.IItemTags.XMATERIAL_TAG;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.cryptomorin.xseries.XMaterial;

import de.ancash.minecraft.serde.impl.AxolotlBucketMetaSerDe;
import de.ancash.minecraft.serde.impl.BannerMetaSerDe;
import de.ancash.minecraft.serde.impl.BookMetaSerDe;
import de.ancash.minecraft.serde.impl.BundleMetaSerDe;
import de.ancash.minecraft.serde.impl.CompassMetaSerDe;
import de.ancash.minecraft.serde.impl.FireworkEffectMetaSerDe;
import de.ancash.minecraft.serde.impl.FireworkMetaSerDe;
import de.ancash.minecraft.serde.impl.IItemDeserializer;
import de.ancash.minecraft.serde.impl.KnowledgeBookMetaSerDe;
import de.ancash.minecraft.serde.impl.LeatherArmorMetaSerDe;
import de.ancash.minecraft.serde.impl.MusicInstrumentMetaSerDe;
import de.ancash.minecraft.serde.impl.PotionMetaSerDe;
import de.ancash.minecraft.serde.impl.SimpleMetaSerDe;
import de.ancash.minecraft.serde.impl.SpawnEggMetaSerDe;
import de.ancash.minecraft.serde.impl.TropicalFishBucketMetaSerDe;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTType;

public class ItemDeserializer {

	public static final ItemDeserializer INSTANCE = new ItemDeserializer();

	private final Set<IItemDeserializer> itemDeserializer = new HashSet<>();

	private ItemDeserializer() {
		itemDeserializer.add(AxolotlBucketMetaSerDe.INSTANCE);
		itemDeserializer.add(BannerMetaSerDe.INSTANCE);
		itemDeserializer.add(BookMetaSerDe.INSTANCE);
		itemDeserializer.add(BundleMetaSerDe.INSTANCE);
		itemDeserializer.add(CompassMetaSerDe.INSTANCE);
		itemDeserializer.add(FireworkEffectMetaSerDe.INSTANCE);
		itemDeserializer.add(FireworkMetaSerDe.INSTANCE);
		itemDeserializer.add(KnowledgeBookMetaSerDe.INSTANCE);
		itemDeserializer.add(LeatherArmorMetaSerDe.INSTANCE);
		itemDeserializer.add(MusicInstrumentMetaSerDe.INSTANCE);
		itemDeserializer.add(PotionMetaSerDe.INSTANCE);
		itemDeserializer.add(SimpleMetaSerDe.INSTANCE);
		itemDeserializer.add(SpawnEggMetaSerDe.INSTANCE);
		itemDeserializer.add(TropicalFishBucketMetaSerDe.INSTANCE);
	}

	public Map<String, Object> deserializeYaml(String s) {
		return deserializeYaml(YamlConfiguration.loadConfiguration(new StringReader(s)));
	}

	public Color deserializeColor(Map<String, Object> map) {
		return Color.fromRGB((int) map.get(RED_TAG), (int) map.get(GREEN_TAG), (int) map.get(BLUE_TAG));
	}

	@SuppressWarnings({ "deprecation", "nls" })
	public NamespacedKey deserializeNamespacedKey(String s) {
		return new NamespacedKey(s.split(":")[0], s.split(":")[1]);
	}

	public PotionEffect deserializePotionEffect(Map<String, Object> effect) {
		return new PotionEffect(PotionEffectType.getByName((String) effect.get(POTION_EFFECT_TYPE_TAG)),
				(int) effect.get(POTION_EFFECT_DURATION_TAG), (int) effect.get(POTION_EFFECT_AMPLIFIER_TAG),
				(boolean) effect.get(POTION_EFFECT_AMBIENT_TAG), (boolean) effect.get(POTION_EFFECT_SHOW_PARTICLES_TAG),
				(boolean) effect.get(POTION_EFFECT_SHOW_ICON_TAG));
	}

	private Map<String, Object> deserializeYaml(ConfigurationSection cs) {
		Map<String, Object> map = new HashMap<>();
		for (String key : cs.getKeys(false))
			if (cs.isConfigurationSection(key))
				map.put(key, deserializeYaml(cs.getConfigurationSection(key)));
			else
				map.put(key, cs.get(key));
		return map;
	}

	@SuppressWarnings("unchecked")
	public FireworkEffect deserializeFireworkEffect(Map<String, Object> map) {
		FireworkEffect.Builder builder = null;
		try {
			builder = FireworkEffect.Builder.class.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		builder.trail((boolean) map.get(FIREWORK_EFFECT_TRAIL_TAG))
				.flicker((boolean) map.get(FIREWORK_EFFECT_FLICKER_TAG))
				.with(FireworkEffect.Type.valueOf((String) map.get(FIREWORK_EFFECT_TYPE_TAG)))
				.withColor(((List<Map<String, Object>>) map.get(FIREWORK_EFFECT_COLORS_TAG)).stream()
						.map(ItemDeserializer.INSTANCE::deserializeColor).collect(Collectors.toList()))
				.withFade(((List<Map<String, Object>>) map.get(FIREWORK_EFFECT_FADE_COLORS_TAG)).stream()
						.map(ItemDeserializer.INSTANCE::deserializeColor).collect(Collectors.toList()));
		return null;
	}

	@SuppressWarnings("unchecked")
	public ItemStack deserializeItemStack(Map<String, Object> map) {
		Optional<XMaterial> opt = XMaterial.matchXMaterial((String) map.remove(XMATERIAL_TAG));
		if (!opt.isPresent())
			throw new IllegalArgumentException();
		ItemStack item = opt.get().parseItem();
		item.setAmount((int) map.remove(AMOUNT_TAG));
		Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
		Entry<String, Object> e = null;
		while (iter.hasNext()) {
			e = iter.next();
			for (IItemDeserializer itd : itemDeserializer)
				if (itd.getKey().equals(e.getKey())) {
					itd.deserialize(item, (Map<String, Object>) map.get(e.getKey()));
					iter.remove();
				}
		}

		NBTItem nbt = new NBTItem(item);
		deserialize(nbt, map);
		nbt.applyNBT(item);
		return item;
	}

	public ItemStack yamlToItemStack(String s) {
		return deserializeItemStack(deserializeYaml(s));
	}

	private void deserialize(NBTCompound compound, Map<String, Object> map) {
		for (String key : map.keySet())
			deserialize(compound, map, key);
	}

	@SuppressWarnings({ "unchecked", "nls" })
	private void deserialize(NBTCompound compound, Map<String, Object> map, String fullKey) {
		String[] keys = fullKey.split(SPLITTER_REGEX);
		String field = keys[0];

		if (keys.length < 2)
			throw new IllegalArgumentException("invalid key " + fullKey);
		if (ITEM_STACK_ARRAY_TAG.equals(keys[1])) {
			List<Map<?, ?>> mapList = (List<Map<?, ?>>) map.get(fullKey);
			ItemStack[] itemArr = new ItemStack[mapList.size()];
			for (int i = 0; i < itemArr.length; i++)
				itemArr[i] = deserializeItemStack((Map<String, Object>) mapList.get(i));
			compound.setItemStackArray(field, itemArr);
			return;
		}

		if (ITEM_STACK_TAG.equals(keys[1])) {
			compound.setItemStack(field, deserializeItemStack((Map<String, Object>) map.get(fullKey)));
			return;
		}

		if (UUID_TAG.equals(keys[1])) {
			compound.setUUID(field, UUID.fromString((String) map.get(fullKey)));
			return;
		}

		if (ITEM_STACK_LIST_TAG.equals(keys[1])) {
			NBTCompoundList list = compound.getCompoundList(field);
			List<Map<String, Object>> items = (List<Map<String, Object>>) map.get(fullKey);
			items.stream().map(this::deserializeItemStack).forEach(i -> {
				NBTContainer temp = new NBTContainer();
				temp.setItemStack(field, i);
				list.addCompound(temp.getCompound(field));
			});
			return;
		}

		NBTType type = NBTType.valueOf(keys[1]);
		if (keys.length == 2) {
			if (type == NBTType.NBTTagCompound) {
				createNBTCompound(compound, (Map<String, Object>) map.get(fullKey), fullKey);
			} else
				set(compound, field, type, map.get(fullKey));
		} else {
			deserializeList(compound, map, fullKey);
		}
	}

	@SuppressWarnings({ "unchecked", "nls" })
	private void deserializeList(NBTCompound compound, Map<String, Object> src, String fullKey) {
		String[] keys = fullKey.split(SPLITTER_REGEX);
		String field = keys[0];
		NBTType listType = NBTType.valueOf(keys[2]);
		switch (listType) {
		case NBTTagCompound:
			NBTCompoundList compoundList = compound.getCompoundList(field);
			List<Map<?, ?>> mapList = (List<Map<?, ?>>) src.get(fullKey);
			for (Map<?, ?> temp : mapList) {
				Map<String, Object> map = (Map<String, Object>) temp;
				writeToCompound(compoundList.addCompound(), map);
			}
			break;
		case NBTTagString:
			NBTList<String> stringList = compound.getStringList(field);
			stringList.addAll((Collection<String>) src.get(fullKey));
			break;
		case NBTTagDouble:
			NBTList<Double> dList = compound.getDoubleList(field);
			dList.addAll((Collection<Double>) src.get(fullKey));
			break;
		case NBTTagInt:
			NBTList<Integer> iList = compound.getIntegerList(field);
			iList.addAll((Collection<Integer>) src.get(fullKey));
			break;
		case NBTTagFloat:
			NBTList<Float> fList = compound.getFloatList(field);
			((Collection<Number>) src.get(fullKey)).stream().map(Number::floatValue).forEach(f -> fList.add(f));
			break;
		case NBTTagLong:
			NBTList<Long> lList = compound.getLongList(field);
			((Collection<Number>) src.get(fullKey)).stream().map(Number::longValue).forEach(l -> lList.add(l));
			break;
		case NBTTagIntArray:
			NBTList<int[]> iaList = compound.getIntArrayList(field);
			for (List<Integer> arr : (List<List<Integer>>) src.get(fullKey))
				iaList.add(arr.stream().mapToInt(Integer::valueOf).toArray());
			break;
		default:
			throw new UnsupportedOperationException(listType + " list not sup");
		}
	}

	private void createNBTCompound(NBTCompound parent, Map<String, Object> map, String fullKey) {
		writeToCompound(parent.addCompound(fullKey.split(SPLITTER_REGEX)[0]), map);
	}

	private void writeToCompound(NBTCompound to, Map<String, Object> map) {
		for (String s : map.keySet())
			deserialize(to, map, s);
	}

	@SuppressWarnings("unchecked")
	private void set(NBTCompound compound, String key, NBTType type, Object value) {
		if (type == NBTType.NBTTagEnd)
			return;
		switch (type) {
		case NBTTagByte:
			compound.setByte(key, (byte) ((int) value));
			break;
		case NBTTagByteArray:
			byte[] arr = new byte[((List<Integer>) value).size()];
			for (int i = 0; i < arr.length; i++)
				arr[i] = (byte) ((int) ((List<Integer>) value).get(i));
			compound.setByteArray(key, arr);
			break;
		case NBTTagDouble:
			compound.setDouble(key, (double) value);
			break;
		case NBTTagFloat:
			compound.setFloat(key, (float) ((double) value));
			break;
		case NBTTagInt:
			compound.setInteger(key, (int) value);
			break;
		case NBTTagLong:
			compound.setLong(key, (long) value);
			break;
		case NBTTagShort:
			compound.setShort(key, (short) (int) value);
			break;
		case NBTTagString:
			compound.setString(key, (String) value);
			break;
		case NBTTagIntArray:
			int[] intArr = new int[((List<Integer>) value).size()];
			for (int i = 0; i < intArr.length; i++)
				intArr[i] = ((List<Integer>) value).get(i);
			compound.setIntArray(key, intArr);
			break;
		case NBTTagCompound:
			compound.mergeCompound((NBTCompound) value);
			break;
		default:
			throw new UnsupportedOperationException(type.name());
		}
	}

	public ItemStack jsonToItemStack(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject obj = reader.readObject();
		YamlConfiguration yaml = new YamlConfiguration();
		add(obj, yaml);
		return yamlToItemStack(yaml.saveToString());
	}

	private void add(JsonObject obj, ConfigurationSection cs) {
		for (String key : obj.keySet()) {
			JsonValue val = obj.get(key);
			switch (val.getValueType()) {
			case ARRAY:
				cs.set(key, toList((JsonArray) val));
				break;
			case FALSE:
				cs.set(key, false);
				break;
			case NULL:
				break;
			case NUMBER:
				cs.set(key, ((JsonNumber) val).numberValue());
				break;
			case OBJECT:
				add((JsonObject) val, cs.createSection(key));
				break;
			case STRING:
				cs.set(key, ((JsonString) val).getChars());
				break;
			case TRUE:
				cs.set(key, true);
				break;
			default:
				break;
			}
		}
	}

	private Object match(JsonValue val) {
		switch (val.getValueType()) {
		case ARRAY:
			return toList((JsonArray) val);
		case FALSE:
			return false;
		case NULL:
			return null;
		case NUMBER:
			return ((JsonNumber) val).numberValue();
		case OBJECT:
			JsonObject obj = (JsonObject) val;
			Map<String, Object> map = new HashMap<>();
			for (String key : obj.keySet())
				map.put(key, match(obj.get(key)));
			return map;
		case STRING:
			return ((JsonString) val).getString();
		case TRUE:
			return true;
		default:
			return null;
		}
	}

	private List<?> toList(JsonArray array) {
		return array.stream().map(this::match).filter(t -> t != null).collect(Collectors.toList());
	}
}
