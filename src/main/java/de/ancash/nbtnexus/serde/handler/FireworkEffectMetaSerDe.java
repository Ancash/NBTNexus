package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerDeStructure;

public class FireworkEffectMetaSerDe implements IItemSerDe {

	public static final FireworkEffectMetaSerDe INSTANCE = new FireworkEffectMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(FIREWORK_EFFECT_TRAIL_TAG, NBTTag.BOOLEAN);
		structure.put(FIREWORK_EFFECT_FLICKER_TAG, NBTTag.BOOLEAN);
		structure.put(FIREWORK_EFFECT_TYPE_TAG, NBTTag.STRING);
		structure.put(FIREWORK_EFFECT_COLORS_TAG, NBTTag.LIST);
		structure.put(FIREWORK_EFFECT_FADE_COLORS_TAG, NBTTag.LIST);
	}

	public SerDeStructure getStructure() {
		return (SerDeStructure) structure.clone();
	}

	FireworkEffectMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
		if (meta.hasEffect()) {
			map = ItemSerializer.INSTANCE.serializeFireworkEffect(meta.getEffect());
			meta.setEffect(null);
		}
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof FireworkEffectMeta;
	}

	@Override
	public String getKey() {
		return FIREWORK_EFFECT_TAG;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.isEmpty())
			return;
		FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
		meta.setEffect(ItemDeserializer.INSTANCE.deserializeFireworkEffect(map));
		item.setItemMeta(meta);
	}
}
