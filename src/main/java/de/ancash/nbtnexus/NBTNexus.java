package de.ancash.nbtnexus;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.Arrays;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.nbtnexus.NBTNexusItem.Type;
import de.ancash.nbtnexus.command.EditCommand;
import de.ancash.nbtnexus.command.NBTNexusCommand;
import de.ancash.nbtnexus.command.SerializeCommand;
import de.ancash.nbtnexus.command.TestSerDeComparisonCommand;
import de.ancash.nbtnexus.packet.InventoryUpdateAdapter;
import de.ancash.nbtnexus.serde.IItemSerDe;
import de.ancash.nbtnexus.serde.handler.AxolotlBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BannerMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BundleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.CompassMetaSerDe;
import de.ancash.nbtnexus.serde.handler.DamageableMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkEffectMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkMetaSerDe;
import de.ancash.nbtnexus.serde.handler.KnowledgeBookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.LeatherArmorMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MapMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MusicInstrumentMetaSerDe;
import de.ancash.nbtnexus.serde.handler.PotionMetaSerDe;
import de.ancash.nbtnexus.serde.handler.RepairableMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SkullMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SpawnEggMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SuspiciousStewMetaSerDe;
import de.ancash.nbtnexus.serde.handler.TropicalFishBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.UnspecificMetaSerDe;
import de.ancash.nbtnexus.serde.structure.SerDeStructure;
import de.ancash.nbtnexus.serde.structure.SerDeStructureEntry;
import de.ancash.nbtnexus.serde.structure.SerDeStructureKeySuggestion;
import de.ancash.nbtnexus.serde.structure.SerDeStructureValueSuggestion;

@SuppressWarnings("deprecation")
public class NBTNexus extends JavaPlugin {

	@SuppressWarnings("nls")
	public static final String SPLITTER = "$";
	@SuppressWarnings("nls")
	public static final String SPLITTER_REGEX = "\\$";

	private ProtocolManager protocolManager;
	private static NBTNexus singleton;
	private NBTNexusCommand cmd;
	private final SerDeStructure structure = new SerDeStructure();
	private InventoryUpdateAdapter ssa;

	public SerDeStructure getStructure() {
		return structure.clone();
	}

	public void registerSerDeStructure(IItemSerDe iisd) {
		if (iisd.getStructure() == null)
			return;
		structure.putMap(iisd.getKey(), iisd.getStructure());
	}

	@SuppressWarnings("nls")
	@Override
	public void onEnable() {
		singleton = this;
		structure.putEntry(AMOUNT_TAG, new SerDeStructureEntry(
				new SerDeStructureKeySuggestion<Byte>(NBTTag.BYTE, a -> a > 0 && a <= 64), null));
		structure.putEntry(XMATERIAL_TAG,
				new SerDeStructureEntry(SerDeStructureKeySuggestion.forEnum(XMaterial.class),
						SerDeStructureValueSuggestion.forEnum(Arrays.asList(XMaterial.VALUES).stream()
								.filter(x -> x.isSupported() && x.parseItem() != null).toArray(XMaterial[]::new))));
		structure.putMap(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG);
		SerDeStructure props = structure.getMap(NBTNexusItem.NBT_NEXUS_ITEM_PROPERTIES_TAG);
		props.putEntry(NBTNexusItem.NBT_NEXUS_ITEM_TYPE_TAG, SerDeStructureEntry.forEnum(Type.class));
		registerSerDeStructure(AxolotlBucketMetaSerDe.INSTANCE);
		registerSerDeStructure(BannerMetaSerDe.INSTANCE);
		registerSerDeStructure(BookMetaSerDe.INSTANCE);
		registerSerDeStructure(BundleMetaSerDe.INSTANCE);
		registerSerDeStructure(CompassMetaSerDe.INSTANCE);
		registerSerDeStructure(DamageableMetaSerDe.INSTANCE);
		registerSerDeStructure(FireworkEffectMetaSerDe.INSTANCE);
		registerSerDeStructure(FireworkMetaSerDe.INSTANCE);
		registerSerDeStructure(KnowledgeBookMetaSerDe.INSTANCE);
		registerSerDeStructure(LeatherArmorMetaSerDe.INSTANCE);
		registerSerDeStructure(MapMetaSerDe.INSTANCE);
		registerSerDeStructure(MusicInstrumentMetaSerDe.INSTANCE);
		registerSerDeStructure(PotionMetaSerDe.INSTANCE);
		registerSerDeStructure(RepairableMetaSerDe.INSTANCE);
		registerSerDeStructure(SkullMetaSerDe.INSTANCE);
		registerSerDeStructure(SpawnEggMetaSerDe.INSTANCE);
		registerSerDeStructure(SuspiciousStewMetaSerDe.INSTANCE);
		registerSerDeStructure(TropicalFishBucketMetaSerDe.INSTANCE);
		registerSerDeStructure(UnspecificMetaSerDe.INSTANCE);
		cmd = new NBTNexusCommand(this);
		cmd.addSubCommand(new EditCommand(this));
		cmd.addSubCommand(new TestSerDeComparisonCommand(this));
		cmd.addSubCommand(new SerializeCommand(this));
		getCommand("nbtn").setExecutor(cmd);
		protocolManager = ProtocolLibrary.getProtocolManager();
		ssa = new InventoryUpdateAdapter(this);
		protocolManager.addPacketListener(ssa);
	}

	@Override
	public void onDisable() {
		protocolManager.removePacketListeners(this);
		ssa.stop();
	}

	public static NBTNexus getInstance() {
		return singleton;
	}
}
