package de.ancash.nbtnexus.serde;

import static de.ancash.nbtnexus.MetaTag.AMOUNT_TAG;
import static de.ancash.nbtnexus.MetaTag.BLUE_TAG;
import static de.ancash.nbtnexus.MetaTag.FIREWORK_EFFECT_COLORS_TAG;
import static de.ancash.nbtnexus.MetaTag.FIREWORK_EFFECT_FADE_COLORS_TAG;
import static de.ancash.nbtnexus.MetaTag.FIREWORK_EFFECT_FLICKER_TAG;
import static de.ancash.nbtnexus.MetaTag.FIREWORK_EFFECT_TRAIL_TAG;
import static de.ancash.nbtnexus.MetaTag.FIREWORK_EFFECT_TYPE_TAG;
import static de.ancash.nbtnexus.MetaTag.GREEN_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_CENTER_X_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_CENTER_Z_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_LOCKED_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_SCALE_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_TRACKING_POSITION_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_UNLIMITED_TRACKING_TAG;
import static de.ancash.nbtnexus.MetaTag.MAP_VIEW_WORLD_TAG;
import static de.ancash.nbtnexus.MetaTag.NBT_NEXUS_ITEM_PROPERTIES_TAG;
import static de.ancash.nbtnexus.MetaTag.NBT_NEXUS_ITEM_TYPE_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_AMBIENT_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_AMPLIFIER_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_DURATION_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_SHOW_ICON_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_SHOW_PARTICLES_TAG;
import static de.ancash.nbtnexus.MetaTag.POTION_EFFECT_TYPE_TAG;
import static de.ancash.nbtnexus.MetaTag.PROPERTY_NAME_TAG;
import static de.ancash.nbtnexus.MetaTag.PROPERTY_SIGNATURE_TAG;
import static de.ancash.nbtnexus.MetaTag.PROPERTY_VALUE_TAG;
import static de.ancash.nbtnexus.MetaTag.RED_TAG;
import static de.ancash.nbtnexus.MetaTag.XMATERIAL_TAG;
import static de.ancash.nbtnexus.NBTNexus.SPLITTER_REGEX;

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

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.nbt.NBTCompound;
import de.ancash.minecraft.nbt.NBTCompoundList;
import de.ancash.minecraft.nbt.NBTContainer;
import de.ancash.minecraft.nbt.NBTItem;
import de.ancash.minecraft.nbt.NBTList;
import de.ancash.nbtnexus.NBTNexusItem.Type;
import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.handler.AxolotlBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BannerMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BundleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.CompassMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkEffectMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkMetaSerDe;
import de.ancash.nbtnexus.serde.handler.KnowledgeBookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.LeatherArmorMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MapMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MusicInstrumentMetaSerDe;
import de.ancash.nbtnexus.serde.handler.PotionMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SimpleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SkullMetaMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SpawnEggMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SuspiciousStewMetaSerDe;
import de.ancash.nbtnexus.serde.handler.TropicalFishBucketMetaSerDe;

@SuppressWarnings("deprecation")
public class ItemDeserializer {

	public static final ItemDeserializer INSTANCE = new ItemDeserializer();

	private final Set<IItemDeserializer> itemDeserializer = new HashSet<>();

	ItemDeserializer() {
		itemDeserializer.add(AxolotlBucketMetaSerDe.INSTANCE);
		itemDeserializer.add(BannerMetaSerDe.INSTANCE);
		itemDeserializer.add(BookMetaSerDe.INSTANCE);
		itemDeserializer.add(BundleMetaSerDe.INSTANCE);
		itemDeserializer.add(CompassMetaSerDe.INSTANCE);
		itemDeserializer.add(FireworkEffectMetaSerDe.INSTANCE);
		itemDeserializer.add(FireworkMetaSerDe.INSTANCE);
		itemDeserializer.add(KnowledgeBookMetaSerDe.INSTANCE);
		itemDeserializer.add(LeatherArmorMetaSerDe.INSTANCE);
		itemDeserializer.add(MapMetaSerDe.INSTANCE);
		itemDeserializer.add(MusicInstrumentMetaSerDe.INSTANCE);
		itemDeserializer.add(PotionMetaSerDe.INSTANCE);
		itemDeserializer.add(SimpleMetaSerDe.INSTANCE);
		itemDeserializer.add(SkullMetaMetaSerDe.INSTANCE);
		itemDeserializer.add(SpawnEggMetaSerDe.INSTANCE);
		itemDeserializer.add(SuspiciousStewMetaSerDe.INSTANCE);
		itemDeserializer.add(TropicalFishBucketMetaSerDe.INSTANCE);
	}

	public void registerDeserializer(IItemDeserializer des) {
		itemDeserializer.add(des);
	}

	public Map<String, Object> deserializeYaml(String s) {
		return deserializeYaml(YamlConfiguration.loadConfiguration(new StringReader(s)));
	}

	public Color deserializeColor(Map<String, Object> map) {
		return Color.fromRGB((int) map.get(RED_TAG), (int) map.get(GREEN_TAG), (int) map.get(BLUE_TAG));
	}

	public MapView deserializeMapView(Map<String, Object> map) {
		MapView view = Bukkit.createMap(Bukkit.getWorld((String) map.get(MAP_VIEW_WORLD_TAG)));
		view.setCenterX((int) map.get(MAP_VIEW_CENTER_X_TAG));
		view.setCenterZ((int) map.get(MAP_VIEW_CENTER_Z_TAG));
		view.setScale(Scale.valueOf((String) map.get(MAP_VIEW_SCALE_TAG)));
		view.setLocked((boolean) map.get(MAP_VIEW_LOCKED_TAG));
		view.setTrackingPosition((boolean) map.get(MAP_VIEW_TRACKING_POSITION_TAG));
		view.setUnlimitedTracking((boolean) map.get(MAP_VIEW_UNLIMITED_TRACKING_TAG));
		return view;
	}

	@SuppressWarnings("unchecked")
	public PropertyMap deserializePropertyMap(Map<String, Object> map) {
		PropertyMap pm = new PropertyMap();
		for (Entry<String, Object> e : map.entrySet())
			pm.putAll(e.getKey(), ((List<Map<String, Object>>) e.getValue()).stream().map(this::deserializeProperty)
					.collect(Collectors.toList()));
		return pm;
	}

	public Property deserializeProperty(Map<String, Object> map) {
		return new Property((String) map.get(PROPERTY_NAME_TAG), (String) map.get(PROPERTY_VALUE_TAG),
				(String) map.get(PROPERTY_SIGNATURE_TAG));
	}

	@SuppressWarnings({ "nls" })
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
		return FireworkEffect.builder().trail((boolean) map.get(FIREWORK_EFFECT_TRAIL_TAG))
				.flicker((boolean) map.get(FIREWORK_EFFECT_FLICKER_TAG))
				.with(FireworkEffect.Type.valueOf((String) map.get(FIREWORK_EFFECT_TYPE_TAG)))
				.withColor(((List<Map<String, Object>>) map.get(FIREWORK_EFFECT_COLORS_TAG)).stream()
						.map(ItemDeserializer.INSTANCE::deserializeColor).collect(Collectors.toList()))
				.withFade(((List<Map<String, Object>>) map.get(FIREWORK_EFFECT_FADE_COLORS_TAG)).stream()
						.map(ItemDeserializer.INSTANCE::deserializeColor).collect(Collectors.toList()))
				.build();
	}

	@SuppressWarnings("unchecked")
	public ItemStack deserializeItemStack(Map<String, Object> map) {
		Map<String, Object> nexus = (Map<String, Object>) map.get(NBT_NEXUS_ITEM_PROPERTIES_TAG);
		if (nexus.get(NBT_NEXUS_ITEM_TYPE_TAG).equals(Type.SERIALIZED.name()))
			map.remove(NBT_NEXUS_ITEM_PROPERTIES_TAG);
		Optional<XMaterial> opt = XMaterial.matchXMaterial((String) map.remove(XMATERIAL_TAG));
		if (!opt.isPresent())
			throw new IllegalArgumentException();
		ItemStack item = opt.get().parseItem();
		item.setAmount((int) map.remove(AMOUNT_TAG));
		Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
		Entry<String, Object> e = null;
		Set<String> remove = new HashSet<>();
		while (iter.hasNext()) {
			e = iter.next();
			for (IItemDeserializer itd : itemDeserializer)
				if (itd.getKey().equals(e.getKey())) {
					if (itd.hasKeysToReverseRelocate()) {
						// relocate(map, itd.getKeysToReverseRelocate());
					}
					itd.deserialize(item, (Map<String, Object>) map.get(e.getKey()));
					remove.add(e.getKey());
				}
		}
		remove.forEach(map::remove);
		NBTItem nbt = new NBTItem(item);
		deserialize(nbt, map);
		nbt.applyNBT(item);
		return item;
	}

//	@SuppressWarnings({ "nls", "unchecked" })
//	private void relocate(Map<String, Object> map, Map<String, String> relocate) {
//		for(Entry<String, String> reloc : relocate.entrySet()) {
//			String[] keys = reloc.getKey().split("\\.");
//			Map<String, Object> tempMap = map;
//			Object orig = null;
//			for(int i = 0; i<keys.length; i++) {
//				if(tempMap.containsKey(keys[i])) {
//					orig = tempMap.get(keys[i]);
//					if(orig instanceof Map)
//						tempMap = (Map<String, Object>) orig;
//					else if(i != keys.length - 1) {
//						tempMap = null;
//						break;
//					}
//				} else{
//					tempMap = null;
//				}
//			}
//			if(tempMap == null)
//				continue;
//			orig = tempMap.remove(keys[keys.length - 1]);
//			tempMap = map;
//			keys = reloc.getValue().split("\\.");
//			for(int i = 0; i<keys.length; i++) {
//				if(i == keys.length - 1) {
//					tempMap.put(keys[i], orig);
//					break;
//				}
//				if((!tempMap.containsKey(keys[i]) || !(tempMap.get(keys[i]) instanceof Map)))
//					tempMap.put(keys[i], new HashMap<>());
//				tempMap = (Map<String, Object>) tempMap.get(keys[i]);
//			}
//		}
//	}

	public ItemStack deserializeYamlToItemStack(String s) {
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

		NBTTag tag = NBTTag.valueOf(keys[1]);

		if (tag == NBTTag.ITEM_STACK_ARRAY) {
			List<Map<?, ?>> mapList = (List<Map<?, ?>>) map.get(fullKey);
			ItemStack[] itemArr = new ItemStack[mapList.size()];
			for (int i = 0; i < itemArr.length; i++)
				itemArr[i] = deserializeItemStack((Map<String, Object>) mapList.get(i));
			compound.setItemStackArray(field, itemArr);
			return;
		}

		if (tag == NBTTag.ITEM_STACK) {
			compound.setItemStack(field, deserializeItemStack((Map<String, Object>) map.get(fullKey)));
			return;
		}

		if (tag == NBTTag.UUID) {
			compound.setUUID(field, UUID.fromString((String) map.get(fullKey)));
			return;
		}

		if (tag == NBTTag.ITEM_STACK_LIST) {
			NBTCompoundList list = compound.getCompoundList(field);
			List<Map<String, Object>> items = (List<Map<String, Object>>) map.get(fullKey);
			items.stream().map(this::deserializeItemStack).forEach(i -> {
				NBTContainer temp = new NBTContainer();
				temp.setItemStack(field, i);
				list.addCompound(temp.getCompound(field));
			});
			return;
		}

		if (keys.length == 2) {
			if (tag == NBTTag.COMPOUND) {
				createNBTCompound(compound, (Map<String, Object>) map.get(fullKey), fullKey);
			} else
				set(compound, field, tag, map.get(fullKey));
		} else {
			deserializeList(compound, map, fullKey);
		}
	}

	@SuppressWarnings({ "unchecked", "nls" })
	private void deserializeList(NBTCompound compound, Map<String, Object> src, String fullKey) {
		String[] keys = fullKey.split(SPLITTER_REGEX);
		String field = keys[0];
		NBTTag listType = NBTTag.valueOf(keys[2]);
		switch (listType) {
		case COMPOUND:
			NBTCompoundList compoundList = compound.getCompoundList(field);
			List<Map<?, ?>> mapList = (List<Map<?, ?>>) src.get(fullKey);
			for (Map<?, ?> temp : mapList) {
				Map<String, Object> map = (Map<String, Object>) temp;
				writeToCompound(compoundList.addCompound(), map);
			}
			break;
		case STRING:
			NBTList<String> stringList = compound.getStringList(field);
			stringList.addAll((Collection<String>) src.get(fullKey));
			break;
		case DOUBLE:
			NBTList<Double> dList = compound.getDoubleList(field);
			dList.addAll((Collection<Double>) src.get(fullKey));
			break;
		case INT:
			NBTList<Integer> iList = compound.getIntegerList(field);
			iList.addAll((Collection<Integer>) src.get(fullKey));
			break;
		case FLOAT:
			NBTList<Float> fList = compound.getFloatList(field);
			((Collection<Number>) src.get(fullKey)).stream().map(Number::floatValue).forEach(f -> fList.add(f));
			break;
		case LONG:
			NBTList<Long> lList = compound.getLongList(field);
			((Collection<Number>) src.get(fullKey)).stream().map(Number::longValue).forEach(l -> lList.add(l));
			break;
		case INT_ARRAY:
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
	private void set(NBTCompound compound, String key, NBTTag type, Object value) {
		if (type == NBTTag.END)
			return;
		switch (type) {
		case BOOLEAN:
			compound.setBoolean(key, (boolean) value);
			break;
		case BYTE:
			compound.setByte(key, (byte) ((int) value));
			break;
		case BYTE_ARRAY:
			byte[] arr = new byte[((List<Integer>) value).size()];
			for (int i = 0; i < arr.length; i++)
				arr[i] = (byte) ((int) ((List<Integer>) value).get(i));
			compound.setByteArray(key, arr);
			break;
		case DOUBLE:
			compound.setDouble(key, (double) value);
			break;
		case FLOAT:
			compound.setFloat(key, (float) ((double) value));
			break;
		case INT:
			compound.setInteger(key, (int) value);
			break;
		case LONG:
			compound.setLong(key, (long) value);
			break;
		case SHORT:
			compound.setShort(key, (short) (int) value);
			break;
		case STRING:
			compound.setString(key, (String) value);
			break;
		case INT_ARRAY:
			int[] intArr = new int[((List<Integer>) value).size()];
			for (int i = 0; i < intArr.length; i++)
				intArr[i] = ((List<Integer>) value).get(i);
			compound.setIntArray(key, intArr);
			break;
		case COMPOUND:
			compound.mergeCompound((NBTCompound) value);
			break;
		default:
			throw new UnsupportedOperationException(type.name());
		}
	}

	public ItemStack deserializeJsonToItemStack(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject obj = reader.readObject();
		YamlConfiguration yaml = new YamlConfiguration();
		add(obj, yaml);
		return deserializeYamlToItemStack(yaml.saveToString());
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

	@SuppressWarnings("nls")
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
			throw new IllegalArgumentException("null type: " + val);
		}
	}

	private List<?> toList(JsonArray array) {
		return array.stream().map(this::match).filter(t -> t != null).collect(Collectors.toList());
	}
}
