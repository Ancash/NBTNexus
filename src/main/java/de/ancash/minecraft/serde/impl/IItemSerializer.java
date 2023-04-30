package de.ancash.minecraft.serde.impl;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

public interface IItemSerializer {

	public Map<String, Object> serialize(ItemStack item);

	public boolean isValid(ItemStack item);

	public String getKey();
}
