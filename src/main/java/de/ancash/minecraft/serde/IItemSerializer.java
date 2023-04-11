package de.ancash.minecraft.serde;

import static de.ancash.minecraft.serde.IItemTags.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.simpleyaml.configuration.file.YamlFile;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTType;

public class IItemSerializer {

//	private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	IItemSerializer() {
	}

	public String toJson(ItemStack is) throws IOException {
		YamlFile yaml = YamlFile.loadConfiguration(() -> new StringReader(toYaml(is)));
		JsonObjectBuilder base = Json.createObjectBuilder();
		Serializer.add(base, yaml);
		return base.build().toString();
	}

	public String toYaml(ItemStack is) throws IOException {
		return Serializer.toYaml(serialize(is));
	}

	public Map<String, Object> serialize(ItemStack is) {
		is = is.clone();
		Map<String, Object> map = new HashMap<>();
		if (!is.getEnchantments().isEmpty()) {
			map.put(ENCHANTMENTS_TAG, serializeEnchantments(is));
			is.getEnchantments().keySet().forEach(is::removeEnchantment);
		}
		ItemMeta meta = is.getItemMeta();
		if (meta != null && (meta.hasLore() || meta.hasDisplayName() || meta.hasLocalizedName())) {
			Map<String, Object> serMeta = new HashMap<>();
			if (meta.hasLore()) {
				serMeta.put(LORE_TAG, meta.getLore());
				meta.setLore(null);
			}
			if (meta.hasDisplayName()) {
				serMeta.put(DISPLAYNAME_TAG, meta.getDisplayName());
				meta.setDisplayName(null);
			}
			if (meta.hasLocalizedName()) {
				serMeta.put(LOCALIZED_NAME_TAG, meta.getLocalizedName());
				meta.setLocalizedName(null);
			}
			map.put(DISPLAY_TAG, serMeta);
			is.setItemMeta(meta);
		}
		if (meta instanceof BookMeta)
			serializeBookMeta(is).forEach(map::put);

		if (meta instanceof BannerMeta)
			serializeBannerMeta(is).forEach(map::put);

		if (meta.hasAttributeModifiers())
			serializeAttributeModifiers(is).forEach(map::put);

		map.put(XMATERIAL_TAG, XMaterial.matchXMaterial(is).name());
		
		if(meta instanceof PotionMeta)
			serializePotionMeta(is).forEach(map::put);;
		
		serializeNBTCompound(new NBTItem(is)).forEach(map::put);
		map.put(AMOUNT_TAG, is.getAmount());
		return map;
	}

	private Map<String, Object> serializePotionMeta(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		List<Map<String, Object>> effects = new ArrayList<>();
		for(PotionEffect effect : meta.getCustomEffects()) {
			Map<String, Object> ser = new HashMap<>();
			ser.put(CUSTOM_POTION_EFFECT_AMPLIFIER_TAG, effect.getAmplifier());
			ser.put(CUSTOM_POTION_EFFECT_DURATION_TAG, effect.getDuration());
			ser.put(CUSTOM_POTION_EFFECT_TYPE_TAG, effect.getType().getName());
			ser.put(CUSTOM_POTION_EFFECT_SHOW_ICON_TAG, effect.hasIcon());
			ser.put(CUSTOM_POTION_EFFECT_SHOW_PARTICLES_TAG, effect.hasParticles());
			ser.put(CUSTOM_POTION_EFFECT_AMBIENT_TAG, effect.isAmbient());
			effects.add(ser);
		}
		map.put(CUSTOM_POTION_EFFECTS_TAG, effects);
		
		PotionData potionData = meta.getBasePotionData();
		Map<String, Object> basePotion = new HashMap<>();
		basePotion.put(BASE_POTION_TYPE_TAG, potionData.getType().name());
		basePotion.put(BASE_POTION_EXTENDED_TAG, potionData.isExtended());
		basePotion.put(BASE_POTION_UPGRADED_TAG, potionData.isUpgraded());
		map.put(BASE_POTION_TAG, basePotion);
		
		Map<String, Object> color = new HashMap<>();
		color.put(RED_TAG, meta.getColor().getRed());
		color.put(GREEN_TAG, meta.getColor().getGreen());
		color.put(BLUE_TAG, meta.getColor().getBlue());
		map.put(POTION_COLOR_TAG, color);
		
		meta.clearCustomEffects();
		item.setItemMeta(meta);
		item.setType(Material.BEDROCK);
		return map;
	}
	
	private Map<String, Object> serializeAttributeModifiers(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> attributes = new ArrayList<>();
		ItemMeta meta = item.getItemMeta();
		for (Entry<Attribute, AttributeModifier> modifier : meta.getAttributeModifiers().entries()) {
			Map<String, Object> ser = new HashMap<>();
			ser.put(ATTRIBUTE_TYPE_TAG, modifier.getKey().name());
			ser.put(ATTRIBUTE_NAME_TAG, modifier.getValue().getName());
			ser.put(ATTRIBUTE_AMOUNT_TAG, modifier.getValue().getAmount());
			ser.put(ATTRIBUTE_OPERATION_TAG, modifier.getValue().getOperation().name());
			ser.put(ATTRIBUTE_UUID_TAG, modifier.getValue().getUniqueId().toString());
			if (modifier.getValue().getSlot() != null)
				ser.put(ATTRIBUTE_SLOT_TAG, modifier.getValue().getSlot().name());
			attributes.add(ser);
		}
		Arrays.stream(Attribute.values()).forEach(meta::removeAttributeModifier);
		item.setItemMeta(meta);
		map.put(ATTRIBUTES_TAG, attributes);
		return map;
	}

	private Map<String, Object> serializeBannerMeta(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		List<Map<String, Object>> patterns = new ArrayList<>();
		BannerMeta meta = (BannerMeta) item.getItemMeta();
		for (Pattern pattern : meta.getPatterns()) {
			Map<String, Object> p = new HashMap<>();
			p.put(BANNER_PATTERN_TYPE_TAG, pattern.getPattern().name());
			p.put(BANNER_PATTERN_COLOR_TAG, pattern.getColor().name());
			patterns.add(p);
		}
		meta.setPatterns(new ArrayList<>());
		item.setItemMeta(meta);
		map.put(BANNER_PATTERNS_TAG, patterns);
		return map;
	}

	private Map<String, Object> serializeBookMeta(ItemStack item) {

		Map<String, Object> map = new HashMap<>();
		BookMeta meta = (BookMeta) item.getItemMeta();
		if (!(meta.hasPages() || meta.hasAuthor() || meta.hasTitle()))
			return map;
		if (meta.hasAuthor()) {
			map.put(BOOK_AUTHOR_TAG, meta.getAuthor());
			meta.setAuthor(null);
		}
		if (meta.hasPages()) {
			map.put(BOOK_PAGES_TAG, meta.getPages());
			meta.setPages(new ArrayList<>());
		}
		if (meta.hasTitle()) {
			map.put(BOOK_TITLE_TAG, meta.getTitle());
			meta.setTitle(null);
		}
		item.setItemMeta(meta);
		return map;
	}

	public List<Map<String, Object>> serializeEnchantments(ItemStack item) {
		Map<Enchantment, Integer> enchs = new HashMap<>();
		item.getEnchantments().forEach(enchs::put);
		List<Map<String, Object>> serializedEnchs = new ArrayList<>();
		for (Entry<Enchantment, Integer> entry : enchs.entrySet()) {
			Map<String, Object> serializedEnch = new HashMap<>();
			serializedEnch.put(ENCHANTMENT_LEVEL_TAG, entry.getValue());
			serializedEnch.put(ENCHANTMENT_TYPE_TAG, XEnchantment.matchXEnchantment(entry.getKey()).name());
			serializedEnchs.add(serializedEnch);
		}
		return serializedEnchs;
	}

	private boolean trySerializeUUID(NBTCompound nbt, String key, Map<String, Object> map) {
		UUID uuid = null;
		try {
			uuid = nbt.getUUID(key);
			if (uuid != null)
				map.put(key + SPLITTER + UUID_TAG, uuid.toString());
		} catch (Exception ex) {

		}
		return uuid != null;
	}

	private boolean trySerializeItemStack(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return false;
		ItemStack item = nbt.getItemStack(key);
		if (item != null && item.getType() != Material.AIR)
			map.put(key + SPLITTER + ITEM_STACK_TAG, serialize(item));
		return item != null && item.getType() != Material.AIR;
	}
	
	private Map<String, Object> trySerializeItemStack(NBTCompound nbt, String key) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return null;
		ItemStack item = nbt.getItemStack(key);
		if (item == null || item.getType() == Material.AIR)
			return null;
		return serialize(item);
	}

	private boolean trySerializeItemStackArray(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return false;
		ItemStack[] itemArr = nbt.getItemStackArray(key);
		if (itemArr != null)
			map.put(key + SPLITTER + ITEM_STACK_ARRAY_TAG,
					Arrays.stream(itemArr).map(this::serialize).collect(Collectors.toList()));
		return itemArr != null;
	}
	
	//what if air and not air
	private boolean trySerializeItemStackList(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagList && nbt.getCompoundList(key).getType() != NBTType.NBTTagCompound)
			return false;
		NBTContainer temp = new NBTContainer();
		NBTCompoundList list = nbt.getCompoundList(key);
		if(list.isEmpty())
			return false;
		List<Map<String, Object>> items = new ArrayList<>();
		for(int i = 0; i<list.size(); i++) {
			temp.getOrCreateCompound(String.valueOf(i)).mergeCompound(list.get(i));
			Map<String, Object> item = trySerializeItemStack(temp, String.valueOf(i));
			if(item == null)
				return false;
			items.add(item);
		}
		map.put(key + SPLITTER + ITEM_STACK_LIST_TAG, items);
		return true;
	}

	public Map<String, Object> serializeNBTCompound(NBTCompound nbt) {
		Map<String, Object> map = new HashMap<>();
		for (String key : nbt.getKeys()) {
			NBTType type = nbt.getType(key);

			if (trySerializeItemStack(nbt, key, map))
				continue;
			if (trySerializeItemStackArray(nbt, key, map))
				continue;
			if (trySerializeItemStackList(nbt, key, map))
				continue;
			if (trySerializeUUID(nbt, key, map))
				continue;

			switch (type) {
			case NBTTagByte:
				map.put(key + SPLITTER + type, nbt.getByte(key));
				break;
			case NBTTagCompound:
				map.put(key + SPLITTER + type, serializeNBTCompound(nbt.getCompound(key)));
				break;
			case NBTTagDouble:
				map.put(key + SPLITTER + type, nbt.getDouble(key));
				break;
			case NBTTagFloat:
				map.put(key + SPLITTER + type, nbt.getFloat(key));
				break;
			case NBTTagInt:
				map.put(key + SPLITTER + type, nbt.getInteger(key));
				break;
			case NBTTagString:
				map.put(key + SPLITTER + type, nbt.getString(key));
				break;
			case NBTTagShort:
				map.put(key + SPLITTER + type, nbt.getShort(key));
				break;
			case NBTTagLong:
				map.put(key + SPLITTER + type, nbt.getLong(key));
				break;
			case NBTTagByteArray:
				List<Byte> byteList = new ArrayList<>();
				for (byte b : nbt.getByteArray(key))
					byteList.add(b);
				map.put(key + SPLITTER + type, byteList);
				break;
			case NBTTagIntArray:
				map.put(key + SPLITTER + type,
						Arrays.stream(nbt.getIntArray(key)).boxed().collect(Collectors.toList()));
				break;
			case NBTTagList:
				map.put(key + SPLITTER + type + SPLITTER + nbt.getListType(key),
						serializeNBTList(nbt, key + SPLITTER + type));
				break;
			default:
				throw new UnsupportedOperationException(type.name());
			}
		}
		return map;
	}

	@SuppressWarnings("nls")
	private List<?> serializeNBTList(NBTCompound nbt, String fullKey) {
		String name = fullKey.split(SPLITTER_REGEX)[0];
		NBTType type = nbt.getListType(name);
		switch (type) {
		case NBTTagCompound:
			NBTCompoundList compounds = nbt.getCompoundList(name);
			List<Map<String, Object>> list = new ArrayList<>();
			for (int i = 0; i < compounds.size(); i++)
				list.add(serializeNBTCompound(compounds.get(i)));
			return list;
		case NBTTagDouble:
			return nbt.getDoubleList(name);
		case NBTTagFloat:
			return nbt.getFloatList(name);
		case NBTTagInt:
			return nbt.getIntegerList(name);
		case NBTTagString:
			return nbt.getStringList(name);
		case NBTTagLong:
			return nbt.getLongList(name);
		case NBTTagIntArray:
			return nbt.getIntArrayList(name);
		default:
			throw new UnsupportedOperationException(type + " list not supported");
		}
	}

//	public Map<String, Object> serializeItemStackMultiThreaded(ItemStack is) {
//		is = is.clone();
//		Map<String, Object> map = new HashMap<>();
//		if (!is.getEnchantments().isEmpty()) {
//			map.put(ENCHANTMENTS_TAG, serializeEnchantments(is));
//			is.getEnchantments().keySet().forEach(is::removeEnchantment);
//		}
//		ItemMeta meta = is.getItemMeta();
//		if (meta != null && (meta.hasLore() || meta.hasDisplayName() || (LOCALIZED_NAME_SUPPORTED && meta.hasLocalizedName()))) {
//			Map<String, Object> serMeta = new HashMap<>();
//			if (meta.hasLore()) {
//				serMeta.put(LORE_TAG, meta.getLore());
//				meta.setLore(null);
//			}
//			if (meta.hasDisplayName()) {
//				serMeta.put(DISPLAYNAME_TAG, meta.getDisplayName());
//				meta.setDisplayName(null);
//			}
//			if (LOCALIZED_NAME_SUPPORTED && meta.hasLocalizedName()) {
//				serMeta.put(LOCALIZED_NAME_TAG, meta.getLocalizedName());
//				meta.setLocalizedName(null);
//			}
//			map.put(DISPLAY_TAG, serMeta);
//			is.setItemMeta(meta);
//		}
//		pool.invoke(new CompoundSerializer(new NBTItem(is))).forEach(map::put);
//		//serializeNBTCompound(new NBTItem(is)).forEach(map::put);
//		map.put(XMATERIAL_TAG, XMaterial.matchXMaterial(is).name());
//		map.put(AMOUNT_TAG, is.getAmount());
//		return map;
//	}
//	
//	public String serializeToYamlMultiThreaded(ItemStack is) {
//		return serializeToYaml(serializeItemStackMultiThreaded(is));
//	}
//	
//	class CompoundSerializer extends RecursiveTask<Map<String, Object>> {
//
//		private static final long serialVersionUID = 5025623847541595160L;
//
//		private final NBTCompound compound;
//		
//		public CompoundSerializer(NBTCompound compound) {
//			this.compound = compound;
//		}
//		
//		@Override
//		protected Map<String, Object> compute() {
//			Map<String, Object> map = new HashMap<>();
//			boolean fork = compound.getKeys().stream().map(compound::getType).filter(t -> t == NBTType.NBTTagCompound).count() > 1;
//			Map<String, CompoundSerializer> tasks = new HashMap<>();
//			for (String key : compound.getKeys()) {
//				NBTType type = compound.getType(key);
//				
//				if(trySerializeItemStack(compound, key, map))
//					continue;
//				if(trySerializeItemStackArray(compound, key, map))
//					continue;
//				if(trySerializeUUID(compound, key, map))
//					continue;
//
//				switch (type) {
//				case NBTTagByte:
//					map.put(key + SPLITTER + type, compound.getByte(key));
//					break;
//				case NBTTagCompound:
//					CompoundSerializer cs = new CompoundSerializer(compound.getCompound(key));
//					if(fork) {
//						tasks.put(key + SPLITTER + type, (CompoundSerializer) pool.submit(cs));
//					} else
//						map.put(key + SPLITTER + type, pool.invoke(cs));
//					break;
//				case NBTTagDouble:
//					map.put(key + SPLITTER + type, compound.getDouble(key));
//					break;
//				case NBTTagFloat:
//					map.put(key + SPLITTER + type, compound.getFloat(key));
//					break;
//				case NBTTagInt:
//					map.put(key + SPLITTER + type, compound.getInteger(key));
//					break;
//				case NBTTagString:
//					map.put(key + SPLITTER + type, compound.getString(key));
//					break;
//				case NBTTagShort:
//					map.put(key + SPLITTER + type, compound.getShort(key));
//					break;
//				case NBTTagLong:
//					map.put(key + SPLITTER + type, compound.getLong(key));
//					break;
//				case NBTTagByteArray:
//					List<Byte> byteList = new ArrayList<>();
//					for (byte b : compound.getByteArray(key))
//						byteList.add(b);
//					map.put(key + SPLITTER + type, byteList);
//					break;
//				case NBTTagIntArray:
//					map.put(key + SPLITTER + type,
//							Arrays.stream(compound.getIntArray(key)).boxed().collect(Collectors.toList()));
//					break;
//				case NBTTagList:
//					map.put(key + SPLITTER + type + SPLITTER + compound.getListType(key),
//							serializeNBTList(compound, key + SPLITTER + type));
//					break;
//				default:
//					throw new UnsupportedOperationException(type.name());
//				}
//			}
//			for(Entry<String, CompoundSerializer> entry : tasks.entrySet())
//				map.put(entry.getKey(), entry.getValue().join());
//			return map;
//		}
//		
//	}
}
