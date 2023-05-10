package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.Tags.GAME_PROFILE_ID_TAG;
import static de.ancash.nbtnexus.Tags.GAME_PROFILE_NAME_TAG;
import static de.ancash.nbtnexus.Tags.GAME_PROFILE_PROPERTIES_TAG;
import static de.ancash.nbtnexus.Tags.GAME_PROFILE_TAG;
import static de.ancash.nbtnexus.Tags.SKULL_NOTE_BLOCK_SOUND_TAG;
import static de.ancash.nbtnexus.Tags.SKULL_TAG;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;

import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.nbt.utils.MinecraftVersion;
import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

@SuppressWarnings("nls")
public class SkullMetaMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final SkullMetaMetaSerDe INSTANCE = new SkullMetaMetaSerDe();
	private static Field gameProfileField;

	static {
		try {
			gameProfileField = ((SkullMeta) XMaterial.PLAYER_HEAD.parseItem().getItemMeta()).getClass()
					.getDeclaredField("profile");
			gameProfileField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	SkullMetaMetaSerDe() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		GameProfile gp = null;
		try {
			gp = ItemStackUtils.getGameProfile(meta);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
		if (gp != null) {
			Map<String, Object> gps = (Map<String, Object>) map.computeIfAbsent(GAME_PROFILE_TAG, k -> new HashMap<>());
			if (gp.getId() != null)
				gps.put(GAME_PROFILE_ID_TAG, gp.getId().toString());
			if (gp.getName() != null)
				gps.put(GAME_PROFILE_NAME_TAG, gp.getName());
			map.put(GAME_PROFILE_PROPERTIES_TAG, ItemSerializer.INSTANCE.serialzePropertyMap(gp.getProperties()));
			try {
				gameProfileField.set(meta, null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_19_R1) && meta.getNoteBlockSound() != null) {
			map.put(SKULL_NOTE_BLOCK_SOUND_TAG,
					ItemSerializer.INSTANCE.serializeNamespacedKey(meta.getNoteBlockSound()));
		}
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof SkullMeta;
	}

	@Override
	public String getKey() {
		return SKULL_TAG;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		if (map.containsKey(GAME_PROFILE_TAG)) {
			Map<String, Object> gps = (Map<String, Object>) map.get(GAME_PROFILE_TAG);
			GameProfile gp = new GameProfile(null, (String) gps.get(GAME_PROFILE_NAME_TAG));
			if (map.containsKey(GAME_PROFILE_PROPERTIES_TAG))
				gp.getProperties().putAll(ItemDeserializer.INSTANCE
						.deserializePropertyMap((Map<String, Object>) map.get(GAME_PROFILE_PROPERTIES_TAG)));
			try {
				gameProfileField.set(meta, gp);
				if (gps.containsKey(GAME_PROFILE_ID_TAG))
					ItemStackUtils.setGameProfileId(meta, UUID.fromString((String) gps.get(GAME_PROFILE_ID_TAG)));
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new IllegalStateException(e);
			}
		}
		if (map.containsKey(SKULL_NOTE_BLOCK_SOUND_TAG))
			meta.setNoteBlockSound(
					ItemDeserializer.INSTANCE.deserializeNamespacedKey((String) map.get(SKULL_NOTE_BLOCK_SOUND_TAG)));
		item.setItemMeta(meta);
	}
}
