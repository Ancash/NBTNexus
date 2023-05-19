package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.structure.SerDeStructure;
import de.ancash.nbtnexus.serde.structure.SerDeStructureEntry;

public class FireworkMetaSerDe implements IItemSerDe {

	public static final FireworkMetaSerDe INSTANCE = new FireworkMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(FIREWORK_POWER_TAG, SerDeStructureEntry.INT);
		structure.putList(FIREWORK_EFFECTS_TAG, NBTTag.COMPOUND);
		SerDeStructure effects = structure.getList(FIREWORK_EFFECTS_TAG);
		effects.put(FIREWORK_EFFECT_TRAIL_TAG, SerDeStructureEntry.BOOLEAN);
		effects.put(FIREWORK_EFFECT_FLICKER_TAG, SerDeStructureEntry.BOOLEAN);
		effects.put(FIREWORK_EFFECT_TYPE_TAG, SerDeStructureEntry.STRING);
		effects.putList(FIREWORK_EFFECT_COLORS_TAG, NBTTag.COMPOUND);
		SerDeStructure color = effects.getList(FIREWORK_EFFECT_COLORS_TAG);
		color.put(RED_TAG, SerDeStructureEntry.INT);
		color.put(GREEN_TAG, SerDeStructureEntry.INT);
		color.put(BLUE_TAG, SerDeStructureEntry.INT);
		effects.putList(FIREWORK_EFFECT_FADE_COLORS_TAG, NBTTag.COMPOUND);
		SerDeStructure fcolor = effects.getList(FIREWORK_EFFECT_COLORS_TAG);
		fcolor.put(RED_TAG, SerDeStructureEntry.INT);
		fcolor.put(GREEN_TAG, SerDeStructureEntry.INT);
		fcolor.put(BLUE_TAG, SerDeStructureEntry.INT);
	}

	public SerDeStructure getStructure() {
		return structure.clone();
	}

	FireworkMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		map.put(FIREWORK_POWER_TAG, meta.getPower());
		if (meta.hasEffects())
			map.put(FIREWORK_EFFECTS_TAG, meta.getEffects().stream()
					.map(ItemSerializer.INSTANCE::serializeFireworkEffect).collect(Collectors.toList()));
		meta.clearEffects();
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof FireworkMeta;
	}

	@Override
	public String getKey() {
		return FIREWORK_TAG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		FireworkMeta meta = (FireworkMeta) item.getItemMeta();
		meta.setPower((int) map.get(FIREWORK_POWER_TAG));
		if (map.containsKey(FIREWORK_EFFECTS_TAG)) {
			meta.addEffects(((List<Map<String, Object>>) map.get(FIREWORK_EFFECTS_TAG)).stream()
					.map(ItemDeserializer.INSTANCE::deserializeFireworkEffect).collect(Collectors.toList()));
		}
		item.setItemMeta(meta);
	}
}
