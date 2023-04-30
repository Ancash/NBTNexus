package de.ancash.minecraft.serde.impl;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

public interface IItemDeserializer {

	public void deserialize(ItemStack item, Map<String, Object> map);

	public String getKey();
}
