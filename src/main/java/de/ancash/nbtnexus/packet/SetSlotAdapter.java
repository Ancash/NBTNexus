package de.ancash.nbtnexus.packet;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import de.ancash.nbtnexus.NBTNexus;

public class SetSlotAdapter extends PacketAdapter {

	private final NBTNexus pl;

	public SetSlotAdapter(NBTNexus pl) {
		super(pl, ListenerPriority.HIGHEST, Arrays.asList(PacketType.Play.Server.SET_SLOT));
		this.pl = pl;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		StructureModifier<ItemStack> itemStackStructureModifier = packet.getItemModifier();
		System.out.println(itemStackStructureModifier.size() + " items");
		for (int i = 0; i < itemStackStructureModifier.size(); i++) {
			ItemStack itemStack = itemStackStructureModifier.read(i);
			if (itemStack != null) {
				Bukkit.getConsoleSender().sendMessage("item: " + itemStack);

				// itemStackStructureModifier.write(i, itemStack);
			}
		}
	}
}
