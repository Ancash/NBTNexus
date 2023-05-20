package de.ancash.nbtnexus.command;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.serde.ItemDeserializer;
import de.ancash.nbtnexus.serde.ItemSerializer;
import de.ancash.nbtnexus.serde.SerializedItem;

public class TestSerDeComparisonCommand extends NBTNexusSubCommand {

	@SuppressWarnings("nls")
	public TestSerDeComparisonCommand(NBTNexus pl) {
		super(pl, "tsdc");
	}

	@SuppressWarnings({ "deprecation", "nls" })
	@Override
	public Boolean apply(CommandSender arg0, String[] arg1) {
		if (!isPlayer(arg0))
			return false;
		Player player = (Player) arg0;
		ItemStack item = player.getItemInHand();
		if (item == null || item.getType() == Material.AIR) {
			player.sendMessage("§cNo item in hand");
			return true;
		}
		String yaml;
		long l = System.nanoTime();
		try {
			yaml = ItemSerializer.INSTANCE.serializeItemStackToYaml(
					ItemDeserializer.INSTANCE.deserializeJsonToItemStack(ItemSerializer.INSTANCE
							.serializeItemStackToJson(ItemDeserializer.INSTANCE.deserializeYamlToItemStack(
									ItemSerializer.INSTANCE.serializeItemStackToYaml(item)))));
		} catch (IOException e) {
			player.sendMessage("§cCould not serialize to yaml");
			e.printStackTrace();
			return true;
		}
		player.sendMessage(
				"§eSerialized item->yaml->item->json->item->json in " + (System.nanoTime() - l) / 1000000d + "ms");
		l = System.nanoTime();
		if (SerializedItem.of(item).isSimilar(ItemDeserializer.INSTANCE.deserializeYamlToItemStack(yaml))) {
			player.sendMessage("§aTest successful! Compared in " + (System.nanoTime() - l) / 1000000d + "ms!");
		} else {
			player.sendMessage("§cTest failed! See console for the data");
			try {
				Bukkit.getConsoleSender().sendMessage(yaml);
				Bukkit.getConsoleSender().sendMessage(ItemSerializer.INSTANCE.serializeItemStackToYaml(item));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		return true;
	}
}
