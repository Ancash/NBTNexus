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
import static de.ancash.minecraft.serde.IItemTags.SPLITTER;
import static de.ancash.minecraft.serde.IItemTags.SPLITTER_REGEX;
import static de.ancash.minecraft.serde.IItemTags.UUID_TAG;
import static de.ancash.minecraft.serde.IItemTags.XMATERIAL_TAG;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.simpleyaml.configuration.file.YamlFile;

import com.cryptomorin.xseries.XMaterial;

import de.ancash.minecraft.serde.impl.AxolotlBucketMetaSerDe;
import de.ancash.minecraft.serde.impl.BannerMetaSerDe;
import de.ancash.minecraft.serde.impl.BookMetaSerDe;
import de.ancash.minecraft.serde.impl.BundleMetaSerDe;
import de.ancash.minecraft.serde.impl.CompassMetaSerDe;
import de.ancash.minecraft.serde.impl.FireworkEffectMetaSerDe;
import de.ancash.minecraft.serde.impl.FireworkMetaSerDe;
import de.ancash.minecraft.serde.impl.IItemSerializer;
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
import de.tr7zw.changeme.nbtapi.NBTType;

public class ItemSerializer {

	public static final ItemSerializer INSTANCE = new ItemSerializer();

//	private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	private final Set<IItemSerializer> itemSerializer = new HashSet<>();

	// private final Set<IItemSerializer> defaultSerializer = new HashSet<>();

	private ItemSerializer() {
		itemSerializer.add(AxolotlBucketMetaSerDe.INSTANCE);
		itemSerializer.add(BannerMetaSerDe.INSTANCE);
		itemSerializer.add(BookMetaSerDe.INSTANCE);
		itemSerializer.add(BundleMetaSerDe.INSTANCE);
		itemSerializer.add(CompassMetaSerDe.INSTANCE);
		itemSerializer.add(FireworkEffectMetaSerDe.INSTANCE);
		itemSerializer.add(FireworkMetaSerDe.INSTANCE);
		itemSerializer.add(KnowledgeBookMetaSerDe.INSTANCE);
		itemSerializer.add(LeatherArmorMetaSerDe.INSTANCE);
		itemSerializer.add(MusicInstrumentMetaSerDe.INSTANCE);
		itemSerializer.add(PotionMetaSerDe.INSTANCE);
		itemSerializer.add(SimpleMetaSerDe.INSTANCE);
		itemSerializer.add(SpawnEggMetaSerDe.INSTANCE);
		itemSerializer.add(TropicalFishBucketMetaSerDe.INSTANCE);
	}

	public void registerSerializer(IItemSerializer ims) {
		itemSerializer.add(ims);
	}

	public Map<String, Object> serialize(Color c) {
		Map<String, Object> map = new HashMap<>();
		map.put(RED_TAG, c.getRed());
		map.put(GREEN_TAG, c.getGreen());
		map.put(BLUE_TAG, c.getBlue());
		return map;
	}

	@SuppressWarnings("nls")
	public String serialize(NamespacedKey key) {
		return key.getNamespace() + ":" + key.getKey();
	}

	public Map<String, Object> serialize(PotionEffect effect) {
		Map<String, Object> ser = new HashMap<>();
		ser.put(POTION_EFFECT_AMPLIFIER_TAG, effect.getAmplifier());
		ser.put(POTION_EFFECT_DURATION_TAG, effect.getDuration());
		ser.put(POTION_EFFECT_TYPE_TAG, effect.getType().getName());
		ser.put(POTION_EFFECT_SHOW_ICON_TAG, effect.hasIcon());
		ser.put(POTION_EFFECT_SHOW_PARTICLES_TAG, effect.hasParticles());
		ser.put(POTION_EFFECT_AMBIENT_TAG, effect.isAmbient());
		return ser;
	}

	public Map<String, Object> serialize(FireworkEffect effect) {
		Map<String, Object> map = new HashMap<>();
		map.put(FIREWORK_EFFECT_TRAIL_TAG, effect.hasTrail());
		map.put(FIREWORK_EFFECT_FLICKER_TAG, effect.hasFlicker());
		map.put(FIREWORK_EFFECT_TYPE_TAG, effect.getType().name());
		map.put(FIREWORK_EFFECT_COLORS_TAG,
				effect.getColors().stream().map(ItemSerializer.INSTANCE::serialize).collect(Collectors.toList()));
		map.put(FIREWORK_EFFECT_FADE_COLORS_TAG,
				effect.getFadeColors().stream().map(ItemSerializer.INSTANCE::serialize).collect(Collectors.toList()));
		return map;
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

		for (IItemSerializer ims : itemSerializer) {
			if (ims.isValid(is))
				map.put(ims.getKey(), ims.serialize(is));
		}
		map.put(XMATERIAL_TAG, XMaterial.matchXMaterial(is).name());
		serializeNBTCompound(new NBTItem(is)).forEach(map::put);
		map.put(AMOUNT_TAG, is.getAmount());
		return map;
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

	// what if air and not air
	private boolean trySerializeItemStackList(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagList && nbt.getCompoundList(key).getType() != NBTType.NBTTagCompound)
			return false;
		NBTContainer temp = new NBTContainer();
		NBTCompoundList list = nbt.getCompoundList(key);
		if (list.isEmpty())
			return false;
		List<Map<String, Object>> items = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			temp.getOrCreateCompound(String.valueOf(i)).mergeCompound(list.get(i));
			Map<String, Object> item = trySerializeItemStack(temp, String.valueOf(i));
			if (item == null)
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
