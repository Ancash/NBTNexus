package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.Map;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import de.ancash.nbtnexus.NBTTag;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerDeStructure;

public class LeatherArmorMetaSerDe implements IItemSerDe {

	public static final LeatherArmorMetaSerDe INSTANCE = new LeatherArmorMetaSerDe();
	private static final SerDeStructure structure = new SerDeStructure();

	static {
		structure.put(RED_TAG, NBTTag.INT);
		structure.put(GREEN_TAG, NBTTag.INT);
		structure.put(BLUE_TAG, NBTTag.INT);
	}

	public SerDeStructure getStructure() {
		return structure.clone();
	}

	LeatherArmorMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		Color c = meta.getColor();
		meta.setColor(null);
		item.setItemMeta(meta);
		return ItemSerializer.INSTANCE.serializeColor(c);
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof LeatherArmorMeta;
	}

	@Override
	public String getKey() {
		return LEATHER_ARMOR_TAG;
	}

	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(ItemDeserializer.INSTANCE.deserializeColor(map));
		item.setItemMeta(meta);
	}

}
