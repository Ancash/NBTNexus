package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.MusicInstrument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.structure.SerDeStructure;
import de.ancash.nbtnexus.serde.structure.SerDeStructureEntry;

public class MusicInstrumentMetaSerDe implements IItemSerDe {

	public static final MusicInstrumentMetaSerDe INSTANCE = new MusicInstrumentMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(MUSIC_INSTRUMENT_TYPE_TAG, SerDeStructureEntry.STRING);
	}

	public SerDeStructure getStructure() {
		return structure.clone();
	}

	MusicInstrumentMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		MusicInstrumentMeta meta = (MusicInstrumentMeta) item.getItemMeta();
		MusicInstrument mi = meta.getInstrument();
		map.put(MUSIC_INSTRUMENT_TYPE_TAG, ItemSerializer.INSTANCE.serializeNamespacedKey(mi.getKey()));
		meta.setInstrument(null);
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return XMaterial.GOAT_HORN.isSupported() && item.getItemMeta() instanceof MusicInstrumentMeta;
	}

	@Override
	public String getKey() {
		return MUSIC_INSTRUMENT_TAG;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		MusicInstrumentMeta meta = (MusicInstrumentMeta) item.getItemMeta();
		meta.setInstrument(MusicInstrument.getByKey(
				ItemDeserializer.INSTANCE.deserializeNamespacedKey((String) map.get(MUSIC_INSTRUMENT_TYPE_TAG))));
		item.setItemMeta(meta);
	}
}
