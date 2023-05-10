package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.SUSPICIOUS_STEW_EFFECTS_TAG;
import static de.ancash.nbtnexus.Tags.SUSPICIOUS_STEW_TAG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SuspiciousStewMeta;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

public class SuspiciousStewMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final SuspiciousStewMetaSerDe INSTANCE = new SuspiciousStewMetaSerDe();

	SuspiciousStewMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		SuspiciousStewMeta meta = (SuspiciousStewMeta) item.getItemMeta();
		if (meta.hasCustomEffects()) {
			map.put(SUSPICIOUS_STEW_TAG, meta.getCustomEffects().stream()
					.map(ItemSerializer.INSTANCE::serializePotionEffect).collect(Collectors.toList()));
			meta.clearCustomEffects();
		}
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return XMaterial.SUSPICIOUS_STEW.isSupported() && item.getItemMeta() instanceof SuspiciousStewMeta;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.containsKey(SUSPICIOUS_STEW_TAG)) {
			SuspiciousStewMeta meta = (SuspiciousStewMeta) item.getItemMeta();
			((List<Map<String, Object>>) map.get(SUSPICIOUS_STEW_EFFECTS_TAG)).stream()
					.map(ItemDeserializer.INSTANCE::deserializePotionEffect)
					.forEach(e -> meta.addCustomEffect(e, true));
			item.setItemMeta(meta);
		}
	}

	@Override
	public String getKey() {
		return SUSPICIOUS_STEW_TAG;
	}

}
