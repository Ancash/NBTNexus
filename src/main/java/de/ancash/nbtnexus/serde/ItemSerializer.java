package de.ancash.nbtnexus.serde;

import static de.ancash.nbtnexus.MetaTag.*;
import static de.ancash.nbtnexus.NBTNexus.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.simpleyaml.configuration.file.YamlFile;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.nbt.NBTCompound;
import de.ancash.minecraft.nbt.NBTCompoundList;
import de.ancash.minecraft.nbt.NBTContainer;
import de.ancash.minecraft.nbt.NBTItem;
import de.ancash.minecraft.nbt.NBTType;
import de.ancash.nbtnexus.NBTNexusItem;
import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.handler.AxolotlBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BannerMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BundleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.CompassMetaSerDe;
import de.ancash.nbtnexus.serde.handler.DamageableMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkEffectMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkMetaSerDe;
import de.ancash.nbtnexus.serde.handler.KnowledgeBookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.LeatherArmorMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MapMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MusicInstrumentMetaSerDe;
import de.ancash.nbtnexus.serde.handler.PotionMetaSerDe;
import de.ancash.nbtnexus.serde.handler.RepairableMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SkullMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SpawnEggMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SuspiciousStewMetaSerDe;
import de.ancash.nbtnexus.serde.handler.TropicalFishBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.UnspecificMetaSerDe;

@SuppressWarnings("deprecation")
public class ItemSerializer {

	public static final ItemSerializer INSTANCE = new ItemSerializer();

//	private final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	private final Set<IItemSerDe> itemSerDe = new HashSet<>();

	// private final Set<IItemSerializer> defaultSerializer = new HashSet<>();

	ItemSerializer() {
		itemSerDe.add(AxolotlBucketMetaSerDe.INSTANCE);
		itemSerDe.add(BannerMetaSerDe.INSTANCE);
		itemSerDe.add(BookMetaSerDe.INSTANCE);
		itemSerDe.add(BundleMetaSerDe.INSTANCE);
		itemSerDe.add(CompassMetaSerDe.INSTANCE);
		itemSerDe.add(FireworkEffectMetaSerDe.INSTANCE);
		itemSerDe.add(FireworkMetaSerDe.INSTANCE);
		itemSerDe.add(KnowledgeBookMetaSerDe.INSTANCE);
		itemSerDe.add(LeatherArmorMetaSerDe.INSTANCE);
		itemSerDe.add(MapMetaSerDe.INSTANCE);
		itemSerDe.add(MusicInstrumentMetaSerDe.INSTANCE);
		itemSerDe.add(PotionMetaSerDe.INSTANCE);
		itemSerDe.add(UnspecificMetaSerDe.INSTANCE);
		itemSerDe.add(SkullMetaSerDe.INSTANCE);
		itemSerDe.add(SpawnEggMetaSerDe.INSTANCE);
		itemSerDe.add(SuspiciousStewMetaSerDe.INSTANCE);
		itemSerDe.add(TropicalFishBucketMetaSerDe.INSTANCE);
		itemSerDe.add(DamageableMetaSerDe.INSTANCE);
		itemSerDe.add(RepairableMetaSerDe.INSTANCE);
	}

	public void registerSerializer(IItemSerDe ims) {
		itemSerDe.add(ims);
	}

	public Map<String, Object> serializeColor(Color c) {
		Map<String, Object> map = new HashMap<>();
		map.put(RED_TAG, c.getRed());
		map.put(GREEN_TAG, c.getGreen());
		map.put(BLUE_TAG, c.getBlue());
		return map;
	}

	@SuppressWarnings("nls")
	public Map<String, Object> serializeMapView(MapView mv) {
		Map<String, Object> map = new HashMap<>();
		map.put(MAP_VIEW_CENTER_X_TAG, mv.getCenterX());
		map.put(MAP_VIEW_CENTER_Z_TAG, mv.getCenterX());
		map.put(MAP_VIEW_SCALE_TAG, mv.getScale().name());
		map.put(MAP_VIEW_WORLD_TAG, mv.getWorld().getName());
		map.put(MAP_VIEW_LOCKED_TAG, mv.isLocked());
		map.put(MAP_VIEW_TRACKING_POSITION_TAG, mv.isTrackingPosition());
		map.put(MAP_VIEW_UNLIMITED_TRACKING_TAG, mv.isUnlimitedTracking());
		if (mv.isVirtual() || mv.getRenderers().size() > 1)
			throw new UnsupportedOperationException("cannot serialize map renderer");
		return map;
	}

	@SuppressWarnings("nls")
	public String serializeNamespacedKey(NamespacedKey key) {
		return key.getNamespace() + ":" + key.getKey();
	}

	public Map<String, Object> serialzePropertyMap(PropertyMap props) {
		Map<String, Object> mprops = new HashMap<>();
		if (!props.isEmpty()) {
			for (Entry<String, Collection<Property>> p : props.asMap().entrySet()) {
				mprops.put(p.getKey(), p.getValue().stream().map(this::serializeProperty).collect(Collectors.toList()));
			}
		}
		return mprops;
	}

	public Map<String, Object> serializeProperty(Property p) {
		Map<String, Object> m = new HashMap<>();
		if (p.hasSignature())
			m.put(PROPERTY_SIGNATURE_TAG, p.getSignature());
		m.put(PROPERTY_NAME_TAG, p.getName());
		m.put(PROPERTY_VALUE_TAG, p.getValue());
		return m;
	}

	public Map<String, Object> serializePotionEffect(PotionEffect effect) {
		Map<String, Object> ser = new HashMap<>();
		ser.put(POTION_EFFECT_AMPLIFIER_TAG, effect.getAmplifier());
		ser.put(POTION_EFFECT_DURATION_TAG, effect.getDuration());
		ser.put(POTION_EFFECT_TYPE_TAG, effect.getType().getName());
		ser.put(POTION_EFFECT_SHOW_ICON_TAG, effect.hasIcon());
		ser.put(POTION_EFFECT_SHOW_PARTICLES_TAG, effect.hasParticles());
		ser.put(POTION_EFFECT_AMBIENT_TAG, effect.isAmbient());
		return ser;
	}

	public Map<String, Object> serializeFireworkEffect(FireworkEffect effect) {
		Map<String, Object> map = new HashMap<>();
		map.put(FIREWORK_EFFECT_TRAIL_TAG, effect.hasTrail());
		map.put(FIREWORK_EFFECT_FLICKER_TAG, effect.hasFlicker());
		map.put(FIREWORK_EFFECT_TYPE_TAG, effect.getType().name());
		map.put(FIREWORK_EFFECT_COLORS_TAG,
				effect.getColors().stream().map(ItemSerializer.INSTANCE::serializeColor).collect(Collectors.toList()));
		map.put(FIREWORK_EFFECT_FADE_COLORS_TAG, effect.getFadeColors().stream()
				.map(ItemSerializer.INSTANCE::serializeColor).collect(Collectors.toList()));
		return map;
	}

	public String serializeItemStackToJson(ItemStack is) throws IOException {
		YamlFile yaml = YamlFile.loadConfiguration(() -> new StringReader(serializeItemStackToYaml(is)));
		JsonObjectBuilder base = Json.createObjectBuilder();
		Serializer.add(base, yaml);
		return base.build().toString();
	}

	public String serializeItemStackToYaml(ItemStack is) throws IOException {
		return Serializer.toYaml(serializeItemStack(is));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> serializeItemStack(ItemStack is) {
		is = is.clone();
		Map<String, Object> map = new HashMap<>();

		Set<String> blacklisted = new HashSet<>();
		map.put(XMATERIAL_TAG, XMaterial.matchXMaterial(is).name());
		map.put(AMOUNT_TAG, is.getAmount());
//		Map<String, String> relocate = new HashMap<>();
		for (IItemSerDe ims : itemSerDe) {
			if (ims.isValid(is)) {
				Map<String, Object> m = ims.serialize(is);
				if (!m.isEmpty())
					map.put(ims.getKey(), m);
//				if (ims.hasKeysToRelocate())
//					relocate.putAll(ims.getKeysToRelocate());
				if (ims.hasBlacklistedKeys())
					blacklisted.addAll(ims.getBlacklistedKeys());
			}
		}
		serializeNBTCompound(new NBTItem(is)).forEach(map::put);
		blacklisted.forEach(map::remove);
		Map<String, Object> nexus = (Map<String, Object>) map
				.computeIfAbsent(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG, k -> new HashMap<>());
		nexus.computeIfAbsent(NBTNexusItem.NBT_NEXUS_ITEM_TYPE_TAG, k -> NBTNexusItem.Type.SERIALIZED.name());
//		relocate(map, relocate);
		return map;
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

	private boolean trySerializeUUID(NBTCompound nbt, String key, Map<String, Object> map) {
		UUID uuid = null;
		try {
			uuid = nbt.getUUID(key);
			if (uuid != null)
				map.put(key + SPLITTER + NBTTag.UUID, uuid.toString());
		} catch (Exception ex) {

		}
		return uuid != null;
	}

	private boolean trySerializeItemStack(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return false;
		ItemStack item = nbt.getItemStack(key);
		if (item != null && item.getType() != Material.AIR)
			map.put(key + SPLITTER + NBTTag.ITEM_STACK, serializeItemStack(item));
		return item != null && item.getType() != Material.AIR;
	}

	private Map<String, Object> trySerializeItemStack(NBTCompound nbt, String key) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return null;
		ItemStack item = nbt.getItemStack(key);
		if (item == null || item.getType() == Material.AIR)
			return null;
		return serializeItemStack(item);
	}

	private boolean trySerializeItemStackArray(NBTCompound nbt, String key, Map<String, Object> map) {
		if (nbt.getType(key) != NBTType.NBTTagCompound)
			return false;
		ItemStack[] itemArr = nbt.getItemStackArray(key);
		if (itemArr != null)
			map.put(key + SPLITTER + NBTTag.ITEM_STACK_ARRAY,
					Arrays.stream(itemArr).map(this::serializeItemStack).collect(Collectors.toList()));
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
		map.put(key + SPLITTER + NBTTag.ITEM_STACK_LIST, items);
		return true;
	}

	public Map<String, Object> serializeNBTCompound(NBTCompound nbt) {
		Map<String, Object> map = new HashMap<>();
		for (String key : nbt.getKeys()) {
			if (trySerializeItemStack(nbt, key, map))
				continue;
			if (trySerializeItemStackArray(nbt, key, map))
				continue;
			if (trySerializeItemStackList(nbt, key, map))
				continue;
			if (trySerializeUUID(nbt, key, map))
				continue;

			NBTTag ntype = NBTTag.getByNBTType(nbt.getType(key));
			switch (ntype) {
			case BYTE:
				map.put(key + SPLITTER + ntype, nbt.getByte(key));
				break;
			case COMPOUND:
				map.put(key + SPLITTER + ntype, serializeNBTCompound(nbt.getCompound(key)));
				break;
			case DOUBLE:
				map.put(key + SPLITTER + ntype, nbt.getDouble(key));
				break;
			case FLOAT:
				map.put(key + SPLITTER + ntype, nbt.getFloat(key));
				break;
			case INT:
				map.put(key + SPLITTER + ntype, nbt.getInteger(key));
				break;
			case STRING:
				map.put(key + SPLITTER + ntype, nbt.getString(key));
				break;
			case SHORT:
				map.put(key + SPLITTER + ntype, nbt.getShort(key));
				break;
			case LONG:
				map.put(key + SPLITTER + ntype, nbt.getLong(key));
				break;
			case BYTE_ARRAY:
				List<Byte> byteList = new ArrayList<>();
				for (byte b : nbt.getByteArray(key))
					byteList.add(b);
				map.put(key + SPLITTER + ntype, byteList);
				break;
			case INT_ARRAY:
				map.put(key + SPLITTER + ntype,
						Arrays.stream(nbt.getIntArray(key)).boxed().collect(Collectors.toList()));
				break;
			case LIST:
				map.put(key + SPLITTER + ntype + SPLITTER + NBTTag.getByNBTType(nbt.getListType(key)),
						serializeNBTList(nbt, key + SPLITTER + ntype));
				break;
			default:
				throw new UnsupportedOperationException(ntype.name());
			}
		}
		return map;
	}

	@SuppressWarnings("nls")
	public List<?> serializeNBTList(NBTCompound nbt, String fullKey) {
		String name = fullKey.split(SPLITTER_REGEX)[0];
		NBTTag type = NBTTag.getByNBTType(nbt.getListType(name));
		switch (type) {
		case COMPOUND:
			NBTCompoundList compounds = nbt.getCompoundList(name);
			List<Map<String, Object>> list = new ArrayList<>();
			for (int i = 0; i < compounds.size(); i++)
				list.add(serializeNBTCompound(compounds.get(i)));
			return list;
		case DOUBLE:
			return nbt.getDoubleList(name);
		case FLOAT:
			return nbt.getFloatList(name);
		case INT:
			return nbt.getIntegerList(name);
		case STRING:
			return nbt.getStringList(name);
		case LONG:
			return nbt.getLongList(name);
		case INT_ARRAY:
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
