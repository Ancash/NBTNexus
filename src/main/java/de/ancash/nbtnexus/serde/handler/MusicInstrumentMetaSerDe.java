package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.MUSIC_INSTRUMENT_TAG;
import static de.ancash.nbtnexus.Tags.MUSIC_INSTRUMENT_TYPE_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.MusicInstrument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

public class MusicInstrumentMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final MusicInstrumentMetaSerDe INSTANCE = new MusicInstrumentMetaSerDe();

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
