package de.ancash.minecraft.serde;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.cryptomorin.xseries.XMaterial;

import de.ancash.minecraft.serde.editor.DisplayEditor;
import de.ancash.minecraft.serde.editor.SkullEditor;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTList;

public class IItems extends JavaPlugin {

	@Override
	public void onEnable() {
		getCommand("serde").setExecutor(this);
		getCommand("deserialize").setExecutor(this);
		ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
		item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		item.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 2);
		item.setAmount(12);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.setLocalizedName("test");
		item.setItemMeta(meta);
		NBTItem nbt = new NBTItem(item);
		nbt.setBoolean("bool", true);
		nbt.setByte("byte", (byte) 12);
		nbt.setByteArray("bytearr", new byte[] { 1, 2, 3 });
		nbt.setDouble("dd", 12d);
		nbt.setFloat("f", 1.2f);
//		nbt.setUUID("uuid", UUID.randomUUID());
		nbt.setIntArray("intaarr", new int[] { 1, 3, 2 });
		nbt.setString("string", "str");
		nbt.setItemStack("is", new ItemStack(Material.BEDROCK, 64));
		NBTCompound comp = nbt.addCompound("test");
		comp.getFloatList("flist").addAll(Arrays.asList(Float.MAX_VALUE, Float.MIN_VALUE));
		comp.getDoubleList("dlist").addAll(Arrays.asList(Double.MAX_VALUE, Double.MIN_VALUE));
		comp.getIntegerList("ilist").addAll(Arrays.asList(Integer.MAX_VALUE, Integer.MIN_VALUE));
		comp.getLongList("llist").addAll(Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE));
		comp.getIntArrayList("intarrrlisst").addAll(Arrays.asList(new int[] { 1 }, new int[] { 2, 3 }));
		comp.getUUIDList("iiddlissst").addAll(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
		comp.getCompoundList("complist").addCompound().addCompound("lol").setString("hello", "there");
		comp.getCompoundList("complist").addCompound().addCompound("laa").getCompoundList("2").addCompound()
				.setString("w", "asdasds");
		NBTList<String> slist = comp.getStringList("blist");
		slist.add("1");
		slist.add("2");
		slist.add("3");
		slist = comp.getStringList("alist");
		slist.add("1");
		slist.add("2");
		slist.add("3");
		for (int i = 0; i < 5; i++) {
			comp = comp.addCompound(i + "");
			comp.setInteger("iterr", i);
			comp.getStringList(i + "-list").addAll(Arrays.asList("lol"));
		}
		for (int a = 0; a < 10; a++) {
			comp.addCompound(String.valueOf(a)).setInteger(String.valueOf(a), a);
		}
		comp.setString("trolll", "muahahah");
		nbt.setItemStackArray("itemarr",
				new ItemStack[] { XMaterial.DIAMOND.parseItem(), XMaterial.OAK_LOG.parseItem() });
		item = nbt.getItem();

		SerializedItem serialized = new SerializedItem(IItemSerDe.SERIALIZER.serialize(item));
		DisplayEditor de = serialized.getDisplayEditor();
		de.setLore("§a§ka§7lol", "§kcb");
		de.setDisplayName("asds");
		SkullEditor se = serialized.getSkullEditor();
		se.setTexture(
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOThmZTk3YjI0MGUwNzkzNGQwOWRiZjhiZDVhMzk3ZTFlMmRjOGE0YTFkY2UzYjExYzVjMTkzNDg3NDRkNDY1NCJ9fX0=");

		try {
			System.out.println("orig: " + item);
			System.out.println("1. yaml: " + IItemSerDe.SERIALIZER.toYaml(item));
			System.out.println("2. yaml: " + IItemSerDe.SERIALIZER
					.toYaml(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item))));
			System.out.println("1. json: " + IItemSerDe.SERIALIZER.toJson(item));
			System.out.println("2. json: " + IItemSerDe.SERIALIZER
					.toJson(IItemSerDe.DESERIALIZER.jsonToItemStack(IItemSerDe.SERIALIZER.toJson(item))));
			System.out.println("alroundd: " + IItemSerDe.DESERIALIZER.jsonToItemStack(IItemSerDe.SERIALIZER
					.toJson(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item)))));
			System.out.println("yaml eq: " + IItemSerDe.SERIALIZER.toYaml(item).equals(IItemSerDe.SERIALIZER
					.toYaml(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item)))));
			System.out.println("json eq: " + IItemSerDe.SERIALIZER.toJson(item).equals(IItemSerDe.SERIALIZER
					.toJson(IItemSerDe.DESERIALIZER.jsonToItemStack(IItemSerDe.SERIALIZER.toJson(item)))));
			checkEnchantedBook();
			checkBook();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void checkBook() throws IOException {
		ItemStack item = XMaterial.WRITTEN_BOOK.parseItem();
		BookMeta meta = (BookMeta) item.getItemMeta();
		meta.setAuthor("§aAncash");
		meta.addPage("line 1", "line 2");
		meta.addPage("line 21", "line 22");
		meta.setTitle("titlaaa");
		item.setItemMeta(meta);
		System.out.println(item);
		System.out.println(IItemSerDe.SERIALIZER.toYaml(item));
		System.out.println(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item)));
		System.out.println(IItemSerDe.SERIALIZER
				.toYaml(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item))));
	}

	private void checkEnchantedBook() throws IOException {
		ItemStack item = XMaterial.ENCHANTED_BOOK.parseItem();
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_DAMAGE, 2, true);
		item.setItemMeta(meta);
		item.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 3);
		System.out.println(item);
		System.out.println(IItemSerDe.SERIALIZER.toYaml(item));
		System.out.println(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item)));
		System.out.println(IItemSerDe.SERIALIZER
				.toYaml(IItemSerDe.DESERIALIZER.yamlToItemStack(IItemSerDe.SERIALIZER.toYaml(item))));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equals("serde")) {
			if (!(sender instanceof Player))
				return false;
			Player player = (Player) sender;
			ItemStack item = player.getItemInHand();
			try {
				if (item == null)
					return false;
				String yaml = IItemSerDe.SERIALIZER.toYaml(item);
				System.out.println(item);
				item = IItemSerDe.DESERIALIZER.yamlToItemStack(yaml);
				String json = IItemSerDe.SERIALIZER.toJson(item);
				System.out.println(json);
				System.out.println(yaml);
				item = IItemSerDe.DESERIALIZER.jsonToItemStack(json);
				System.out.println(item);
				player.getInventory().addItem(item);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (command.getName().equals("deserialize")) {
			String data = String.join(" ", args);
			ItemStack item = null;
			try {
				item = IItemSerDe.DESERIALIZER.jsonToItemStack(data);
				for (Player p : Bukkit.getOnlinePlayers())
					p.getInventory().addItem(item);
				System.out.println("yaml: " + IItemSerDe.SERIALIZER.toYaml(item));
				System.out.println("json: " + IItemSerDe.SERIALIZER.toJson(item));
			} catch (Exception e) {
				try {
					item = IItemSerDe.DESERIALIZER.yamlToItemStack(data);
					for (Player p : Bukkit.getOnlinePlayers())
						p.getInventory().addItem(item);
					System.out.println("yaml: " + IItemSerDe.SERIALIZER.toYaml(item));
					System.out.println("json: " + IItemSerDe.SERIALIZER.toJson(item));
				} catch (Exception e2) {
					sender.sendMessage(e.getMessage());
					sender.sendMessage(e2.getMessage());
					return false;
				}
			}
			
			return true;
		}
		return true;
	}
}
