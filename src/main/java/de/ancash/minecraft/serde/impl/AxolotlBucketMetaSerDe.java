package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.AXOLOTL_BUCKET_TAG;
import static de.ancash.minecraft.serde.IItemTags.AXOLOTL_BUCKET_VARIANT_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Axolotl.Variant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;

import com.cryptomorin.xseries.XMaterial;

public class AxolotlBucketMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final AxolotlBucketMetaSerDe INSTANCE = new AxolotlBucketMetaSerDe();

	AxolotlBucketMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		AxolotlBucketMeta meta = (AxolotlBucketMeta) item.getItemMeta();
		if (meta.hasVariant())
			map.put(AXOLOTL_BUCKET_VARIANT_TAG, meta.getVariant().name());
		meta.setVariant(null);
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return XMaterial.AXOLOTL_BUCKET.isSupported() && item.getItemMeta() instanceof AxolotlBucketMeta;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.containsKey(AXOLOTL_BUCKET_VARIANT_TAG)) {
			AxolotlBucketMeta meta = (AxolotlBucketMeta) item.getItemMeta();
			meta.setVariant(Variant.valueOf((String) map.get(AXOLOTL_BUCKET_VARIANT_TAG)));
			item.setItemMeta(meta);
		}
	}

	@Override
	public String getKey() {
		return AXOLOTL_BUCKET_TAG;
	}

}
