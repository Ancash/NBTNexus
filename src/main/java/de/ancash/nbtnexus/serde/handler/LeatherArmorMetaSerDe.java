package de.ancash.nbtnexus.serde.handler;

import static de.ancash.nbtnexus.MetaTag.LEATHER_ARMOR_TAG;

import java.util.Map;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import de.ancash.nbtnexus.serde.IItemDeserializer;
import de.ancash.nbtnexus.serde.IItemSerializer;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;

public class LeatherArmorMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final LeatherArmorMetaSerDe INSTANCE = new LeatherArmorMetaSerDe();

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
