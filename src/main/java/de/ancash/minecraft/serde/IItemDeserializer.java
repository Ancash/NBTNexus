package de.ancash.minecraft.serde;

import static de.ancash.minecraft.serde.IItemTags.*;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;
import de.tr7zw.changeme.nbtapi.NBTType;

public class IItemDeserializer {

	IItemDeserializer() {

	}

	public Map<String, Object> deserializeYaml(String s) {
		return deserializeYaml(YamlConfiguration.loadConfiguration(new StringReader(s)));
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
	public ItemStack deserializeItemStack(Map<String, Object> map) {
		Optional<XMaterial> opt = XMaterial.matchXMaterial((String) map.remove(XMATERIAL_TAG));
		if (!opt.isPresent())
			throw new IllegalArgumentException();
		ItemStack item = opt.get().parseItem();
		item.setAmount((int) map.remove(AMOUNT_TAG));

		if (map.containsKey(ENCHANTMENTS_TAG)) {
			List<Map<String, Object>> enchs = (List<Map<String, Object>>) map.remove(ENCHANTMENTS_TAG);
			for (Map<String, Object> ench : enchs) {
				item.addUnsafeEnchantment(
						XEnchantment.matchXEnchantment((String) ench.get(ENCHANTMENT_TYPE_TAG)).get().getEnchant(),
						(int) ench.get(ENCHANTMENT_LEVEL_TAG));
			}
		}

		if (map.containsKey(DISPLAY_TAG)) {
			Map<String, Object> serMeta = (Map<String, Object>) map.remove(DISPLAY_TAG);
			ItemMeta meta = item.getItemMeta();
			meta.setLore((List<String>) serMeta.get(LORE_TAG));
			meta.setDisplayName((String) serMeta.get(DISPLAYNAME_TAG));
			meta.setLocalizedName((String) serMeta.get(LOCALIZED_NAME_TAG));
			item.setItemMeta(meta);
		}

		if (item.getItemMeta() instanceof BookMeta)
			deserializeBookMeta(item, map);

		if (item.getItemMeta() instanceof BannerMeta)
			deserializeBannerMeta(item, map);

		if (map.containsKey(ATTRIBUTES_TAG))
			deserializeAttributeModifiers(item, map);

		if (item.getItemMeta() instanceof PotionMeta)
			deserializePotionMeta(item, map);
		NBTItem nbt = new NBTItem(item);
		deserialize(nbt, map);
		nbt.applyNBT(item);
		return item;
	}

	@SuppressWarnings("unchecked")
	private void deserializePotionMeta(ItemStack item, Map<String, Object> map) {
		if (!map.containsKey(CUSTOM_POTION_EFFECTS_TAG))
			return;
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		for (Map<String, Object> effect : (List<Map<String, Object>>) map.remove(CUSTOM_POTION_EFFECTS_TAG)) {
			meta.addCustomEffect(
					new PotionEffect(PotionEffectType.getByName((String) effect.get(CUSTOM_POTION_EFFECT_TYPE_TAG)),
							(int) effect.get(CUSTOM_POTION_EFFECT_DURATION_TAG),
							(int) effect.get(CUSTOM_POTION_EFFECT_AMPLIFIER_TAG),
							(boolean) effect.get(CUSTOM_POTION_EFFECT_AMBIENT_TAG),
							(boolean) effect.get(CUSTOM_POTION_EFFECT_SHOW_PARTICLES_TAG),
							(boolean) effect.get(CUSTOM_POTION_EFFECT_SHOW_ICON_TAG)),
					true);
		}
		Map<String, Object> potionBase = (Map<String, Object>) map.remove(BASE_POTION_TAG);
		meta.setBasePotionData(new PotionData(PotionType.valueOf((String) potionBase.get(BASE_POTION_TYPE_TAG)),
				(boolean) potionBase.get(BASE_POTION_EXTENDED_TAG),
				(boolean) potionBase.get(BASE_POTION_UPGRADED_TAG)));
		
		Map<String, Object> color = (Map<String, Object>) map.remove(POTION_COLOR_TAG);
		
		meta.setColor(Color.fromRGB((int) color.remove(RED_TAG), (int) color.remove(GREEN_TAG), (int) color.remove(BLUE_TAG)));
		
		item.setItemMeta(meta);
	}

	@SuppressWarnings("unchecked")
	private void deserializeAttributeModifiers(ItemStack item, Map<String, Object> map) {
		ItemMeta meta = item.getItemMeta();
		for (Map<String, Object> attribute : (List<Map<String, Object>>) map.remove(ATTRIBUTES_TAG)) {
			meta.addAttributeModifier(Attribute.valueOf((String) attribute.get(ATTRIBUTE_TYPE_TAG)),
					new AttributeModifier(UUID.fromString((String) attribute.get(ATTRIBUTE_UUID_TAG)),
							(String) attribute.get(ATTRIBUTE_NAME_TAG), (double) attribute.get(ATTRIBUTE_AMOUNT_TAG),
							Operation.valueOf((String) attribute.get(ATTRIBUTE_OPERATION_TAG)),
							attribute.containsKey(ATTRIBUTE_SLOT_TAG)
									? EquipmentSlot.valueOf((String) attribute.get(ATTRIBUTE_SLOT_TAG))
									: null));
		}
		item.setItemMeta(meta);
	}

	@SuppressWarnings("unchecked")
	private void deserializeBannerMeta(ItemStack item, Map<String, Object> map) {
		BannerMeta bm = (BannerMeta) item.getItemMeta();
		List<Map<String, Object>> patterns = (List<Map<String, Object>>) map.remove(BANNER_PATTERNS_TAG);
		for (Map<String, Object> pattern : patterns)
			bm.addPattern(new Pattern(DyeColor.valueOf((String) pattern.get(BANNER_PATTERN_COLOR_TAG)),
					PatternType.valueOf((String) pattern.get(BANNER_PATTERN_TYPE_TAG))));
		item.setItemMeta(bm);
	}

	@SuppressWarnings("unchecked")
	private void deserializeBookMeta(ItemStack item, Map<String, Object> map) {
		BookMeta bm = (BookMeta) item.getItemMeta();
		bm.setAuthor((String) map.remove(BOOK_AUTHOR_TAG));
		bm.setTitle((String) map.remove(BOOK_TITLE_TAG));
		if (map.containsKey(BOOK_PAGES_TAG))
			bm.setPages((List<String>) map.remove(BOOK_PAGES_TAG));
		item.setItemMeta(bm);
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
