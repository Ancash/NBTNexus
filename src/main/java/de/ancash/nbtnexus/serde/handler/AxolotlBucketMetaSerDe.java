package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.AXOLOTL_BUCKET_AGE_TAG;
import static de.ancash.nbtnexus.Tags.AXOLOTL_BUCKET_HEALTH_TAG;
import static de.ancash.nbtnexus.Tags.AXOLOTL_BUCKET_TAG;
import static de.ancash.nbtnexus.Tags.AXOLOTL_BUCKET_VARIANT_TAG;
import static de.ancash.nbtnexus.Tags.SPLITTER;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Axolotl.Variant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.nbt.NBTType;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;

public class AxolotlBucketMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final AxolotlBucketMetaSerDe INSTANCE = new AxolotlBucketMetaSerDe();

	private final Set<String> bl = new HashSet<>();
	private final Map<String, String> relocate = new HashMap<>();
	private final Map<String, String> reverseRelocate = new HashMap<>();

	@SuppressWarnings("nls")
	AxolotlBucketMetaSerDe() {
		bl.add("Variant" + SPLITTER + NBTType.NBTTagInt.name());
		relocate.put("Age" + SPLITTER + NBTType.NBTTagInt.name(), getKey() + "." + AXOLOTL_BUCKET_AGE_TAG);
		relocate.put("Health" + SPLITTER + NBTType.NBTTagFloat.name(), getKey() + "." + AXOLOTL_BUCKET_HEALTH_TAG);
		reverseRelocate.put(getKey() + "." + AXOLOTL_BUCKET_HEALTH_TAG,
				"Health" + SPLITTER + NBTType.NBTTagFloat.name());
		reverseRelocate.put(getKey() + "." + AXOLOTL_BUCKET_AGE_TAG, "Age" + SPLITTER + NBTType.NBTTagInt.name());
	}

	@Override
	public Map<String, String> getKeysToRelocate() {
		return Collections.unmodifiableMap(relocate);
	}

	@Override
	public Map<String, String> getKeysToReverseRelocate() {
		return reverseRelocate;
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
	public Set<String> getBlacklistedKeys() {
		return bl;
	}

	@Override
	public String getKey() {
		return AXOLOTL_BUCKET_TAG;
	}

}
