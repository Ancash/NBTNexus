package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.TROPICAL_FISH_BUCKET_BODY_COLOR_TAG;
import static de.ancash.minecraft.serde.IItemTags.TROPICAL_FISH_BUCKET_PATTERN_COLOR_TAG;
import static de.ancash.minecraft.serde.IItemTags.TROPICAL_FISH_BUCKET_PATTERN_TAG;
import static de.ancash.minecraft.serde.IItemTags.TROPICAL_FISH_BUCKET_TAG;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

import com.cryptomorin.xseries.XMaterial;

public class TropicalFishBucketMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final TropicalFishBucketMetaSerDe INSTANCE = new TropicalFishBucketMetaSerDe();

	TropicalFishBucketMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		TropicalFishBucketMeta meta = (TropicalFishBucketMeta) item.getItemMeta();
		if (meta.hasVariant()) {
			map.put(TROPICAL_FISH_BUCKET_BODY_COLOR_TAG, meta.getBodyColor().name());
			map.put(TROPICAL_FISH_BUCKET_PATTERN_COLOR_TAG, meta.getPatternColor().name());
			map.put(TROPICAL_FISH_BUCKET_PATTERN_TAG, meta.getPattern().name());
		}
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return XMaterial.TROPICAL_FISH_BUCKET.isSupported() && item.getItemMeta() instanceof TropicalFishBucketMeta;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		if (map.containsKey(TROPICAL_FISH_BUCKET_BODY_COLOR_TAG)) {
			TropicalFishBucketMeta meta = (TropicalFishBucketMeta) item.getItemMeta();
			meta.setBodyColor(DyeColor.valueOf((String) map.get(TROPICAL_FISH_BUCKET_BODY_COLOR_TAG)));
			meta.setPatternColor(DyeColor.valueOf((String) map.get(TROPICAL_FISH_BUCKET_PATTERN_COLOR_TAG)));
			meta.setPattern(Pattern.valueOf((String) map.get(TROPICAL_FISH_BUCKET_PATTERN_TAG)));
			item.setItemMeta(meta);
		}
	}

	@Override
	public String getKey() {
		return TROPICAL_FISH_BUCKET_TAG;
	}

}
