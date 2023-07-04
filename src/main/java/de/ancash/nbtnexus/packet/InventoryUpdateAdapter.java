package de.ancash.nbtnexus.packet;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.PhasedBackoffWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

import de.ancash.disruptor.MultiConsumerDisruptor;
import de.ancash.nbtnexus.NBTNexus;
import de.ancash.nbtnexus.serde.ItemSerializer;

class QueueEvent {
	Runnable r;
}

class QueueEventListener implements EventHandler<QueueEvent> {

	@Override
	public void onEvent(QueueEvent arg0, long arg1, boolean arg2) throws Exception {
		arg0.r.run();
	}
}

public class InventoryUpdateAdapter extends PacketAdapter {

	private final NBTNexus pl;
	@SuppressWarnings("nls")
	private final String metaKey = "NBTNexusItemComputable";
	private final MultiConsumerDisruptor<QueueEvent> disruptor = new MultiConsumerDisruptor<QueueEvent>(QueueEvent::new,
			1024, ProducerType.MULTI,
			new PhasedBackoffWaitStrategy(0, 100_000, TimeUnit.NANOSECONDS, new BlockingWaitStrategy()),
			IntStream.range(0, Runtime.getRuntime().availableProcessors()).boxed().map(i -> new QueueEventListener())
					.toArray(QueueEventListener[]::new));

	public InventoryUpdateAdapter(NBTNexus pl) {
		super(pl, ListenerPriority.HIGHEST,
				Arrays.asList(PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS));
		this.pl = pl;
	}

	private void scheduleItemComputation(StructureModifier<ItemStack> itemStackStructureModifier, Player player,
			PacketContainer container, boolean filtered) {
		disruptor.publishEvent((e,
				seq) -> e.r = () -> scheduleItemComputation0(itemStackStructureModifier, player, container, filtered));
	}

	public void stop() {
		disruptor.stop();
	}

	@SuppressWarnings("nls")
	private void scheduleItemComputation0(StructureModifier<ItemStack> itemStackStructureModifier, Player player,
			PacketContainer container, boolean filtered) {
		for (int i = 0; i < itemStackStructureModifier.size(); i++) {
			ItemStack itemStack = itemStackStructureModifier.read(i);
			if (itemStack != null && itemStack.getType() != Material.AIR) {
				try {
					itemStackStructureModifier.write(i, ItemComputerRegistry.computeItem(itemStack));
				} catch (Throwable th) {
					pl.getLogger().severe("Could not compute item, using already set item");
					th.printStackTrace();
					pl.getLogger().severe(ItemSerializer.INSTANCE.serializeItemStack(itemStack).toString());
					itemStackStructureModifier.write(i, itemStack);
				}
			}
		}

		ProtocolLibrary.getProtocolManager().sendServerPacket(player, container, filtered);
	}

	private void scheduleItemListComputation(StructureModifier<List<ItemStack>> itemStackStructureModifier,
			Player player, PacketContainer container, boolean filtered) {
		disruptor.publishEvent((e, seq) -> e.r = () -> scheduleItemListComputation0(itemStackStructureModifier, player,
				container, filtered));
	}

	@SuppressWarnings("nls")
	private void scheduleItemListComputation0(StructureModifier<List<ItemStack>> itemStackStructureModifier,
			Player player, PacketContainer container, boolean filtered) {
		for (int i = 0; i < itemStackStructureModifier.size(); i++) {
			List<ItemStack> list = itemStackStructureModifier.read(i);
			for (int slot = 0; slot < list.size(); slot++) {
				ItemStack itemStack = list.get(slot);
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					try {
						list.set(slot, ItemComputerRegistry.computeItem(itemStack));
					} catch (Throwable th) {
						pl.getLogger().severe("Could not compute list item, using already set item");
						th.printStackTrace();
						pl.getLogger().severe(ItemSerializer.INSTANCE.serializeItemStack(itemStack).toString());
					}
				}
			}
			itemStackStructureModifier.write(i, list);
		}
		ProtocolLibrary.getProtocolManager().sendServerPacket(player, container, filtered);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE
				|| event.getPlayer().getGameMode() == GameMode.SPECTATOR)
			return;
		if (!packet.getMeta(metaKey).isPresent()) {
			packet.setMeta(metaKey, System.nanoTime());
			event.setCancelled(true);
			if (event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS)) {
				scheduleItemListComputation(packet.getItemListModifier(), event.getPlayer(), packet,
						event.isFiltered());
			} else {
				scheduleItemComputation(packet.getItemModifier(), event.getPlayer(), packet, event.isFiltered());
			}
		} else {
			packet.removeMeta(metaKey);
			return;
		}
	}
}
