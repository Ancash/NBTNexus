package de.ancash.nbtnexus.packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
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

public class InventoryUpdateAdapter extends PacketAdapter implements Listener {

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

	public void stop() {
		disruptor.stop();
	}

	private void computeItemAsync(StructureModifier<ItemStack> itemStackStructureModifier, Player player,
			PacketContainer container, boolean filtered) {
		disruptor.publishEvent((e, seq) -> e.r = () -> {
			computeItem(itemStackStructureModifier, player);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, container, filtered);
		});
	}

	@SuppressWarnings("nls")
	private void computeItem(StructureModifier<ItemStack> itemStackStructureModifier, Player player) {
		ItemStack original = itemStackStructureModifier.read(0);
		if (original != null && original.getType() != Material.AIR) {
			try {
				itemStackStructureModifier.write(0, ItemComputerRegistry.computeItem(original));
			} catch (Throwable th) {
				pl.getLogger().severe("Could not compute item, using already set item");
				th.printStackTrace();
				try {
					pl.getLogger().severe(ItemSerializer.INSTANCE.serializeItemStack(original).toString());
				} catch (Throwable th2) {
					pl.getLogger().severe(original.toString());
				}
				itemStackStructureModifier.write(0, original);
			}
		}
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			itemStackStructureModifier.write(0, original);
	}

	private void scheduleItemListComputationAsync(StructureModifier<List<ItemStack>> itemStackStructureModifier,
			Player player, PacketContainer container, boolean filtered) {
		disruptor.publishEvent((e, seq) -> e.r = () -> {
			computeItemList(itemStackStructureModifier, player);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, container, filtered);
		});
	}

	@SuppressWarnings({ "nls" })
	private void computeItemList(StructureModifier<List<ItemStack>> itemStackStructureModifier, Player player) {
		List<List<ItemStack>> original = new ArrayList<>();
		for (int i = 0; i < itemStackStructureModifier.size(); i++) {
			List<ItemStack> list = itemStackStructureModifier.read(i);
			original.add(list);
			for (int slot = 0; slot < list.size(); slot++) {
				ItemStack itemStack = list.get(slot);
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					try {
						list.set(slot, ItemComputerRegistry.computeItem(itemStack));
					} catch (Throwable th) {
						pl.getLogger().severe("Could not compute list item, using already set item");
						th.printStackTrace();
						try {
							pl.getLogger().severe(ItemSerializer.INSTANCE.serializeItemStack(itemStack).toString());
						} catch (Throwable th2) {
							pl.getLogger().severe(original.toString());
						}
					}
				}
			}
			itemStackStructureModifier.write(i, list);
		}
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			for (int i = 0; i < original.size(); i++)
				itemStackStructureModifier.write(i, original.get(i));
	}

	@org.bukkit.event.EventHandler
	public void onGMChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if ((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
				&& (event.getNewGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.SPECTATOR)) {
			Bukkit.getScheduler().runTaskLater(pl, () -> {
				if (!event.isCancelled())
					player.updateInventory();
			}, 1);
		}
	}

	@org.bukkit.event.EventHandler
	public void onPickUp(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Bukkit.getScheduler().runTaskLater(pl, () -> {
			if (!event.isCancelled())
				((Player) event.getEntity()).updateInventory();
		}, 1);
	}

	private void handleAsync(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		if (!packet.getMeta(metaKey).isPresent()) {
			packet.setMeta(metaKey, System.nanoTime());
			if (event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS)) {
				event.setCancelled(true);
				scheduleItemListComputationAsync(packet.getItemListModifier(), event.getPlayer(), packet,
						event.isFiltered());
			} else {
				event.setCancelled(true);
				computeItemAsync(packet.getItemModifier(), event.getPlayer(), packet, event.isFiltered());
			}
		} else
			packet.removeMeta(metaKey);
	}

	private void handleSync(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		if (event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS))
			computeItemList(packet.getItemListModifier(), event.getPlayer());
		else
			computeItem(packet.getItemModifier(), event.getPlayer());
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE
				|| event.getPlayer().getGameMode() == GameMode.SPECTATOR)
			return;
		if (packet.getIntegers().read(0) != 0) // ignore everything that is not player inv window id (0)
			return;

		if (pl.editPacketsSync())
			handleSync(event);
		else
			handleAsync(event);

	}
}
