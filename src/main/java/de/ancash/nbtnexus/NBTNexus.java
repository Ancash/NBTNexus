package de.ancash.nbtnexus;

import static de.ancash.nbtnexus.MetaTag.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

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
import de.ancash.nbtnexus.serde.handler.EnchantmentStorageMetaSerDe;
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
	private InventoryUpdateAdapter iua;
	@SuppressWarnings("nls")
	private final YamlFile config = new YamlFile(new File("plugins/NBTNexus/config.yml"));
	private boolean enableExperimentalPacketEditing = false;
	private boolean packetEditingSync = false;

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
		try {
			loadConfig();
		} catch (IOException e) {
			getLogger().severe("Could not load config");
			e.printStackTrace();
		}
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
		registerSerDeStructure(EnchantmentStorageMetaSerDe.INSTANCE);
		cmd = new NBTNexusCommand(this);
		cmd.addSubCommand(new EditCommand(this));
		cmd.addSubCommand(new TestSerDeComparisonCommand(this));
		cmd.addSubCommand(new SerializeCommand(this));
		getCommand("nbtn").setExecutor(cmd);
		addPacketListener();
	}

	public boolean enableExperimentalPacketEditing() {
		return enableExperimentalPacketEditing;
	}

	public boolean editPacketsSync() {
		return packetEditingSync;
	}

	@SuppressWarnings("nls")
	private void addPacketListener() {
		protocolManager = ProtocolLibrary.getProtocolManager();
		if (!enableExperimentalPacketEditing) {
			getLogger().info("Experimental editing of items in packets is disabled");
			return;
		}
		getLogger().warning("Experimental editing of items in packets is enabled");
		getLogger().info("Editing packets " + (packetEditingSync ? "sync" : "async"));
		iua = new InventoryUpdateAdapter(this);
		protocolManager.addPacketListener(iua);
		Bukkit.getPluginManager().registerEvents(iua, singleton);
	}

	@SuppressWarnings("nls")
	private void loadConfig() throws InvalidConfigurationException, IOException {
		config.createNewFile(false);
		config.loadWithComments();
		checkFile(config, "config.yml");
		config.loadWithComments();
		enableExperimentalPacketEditing = config.getBoolean("enable-experimental-packet-editing");
		packetEditingSync = config.getBoolean("experimental-packet-editing-sync");
	}

	@SuppressWarnings("nls")
	private void checkFile(YamlFile file, String src)
			throws org.simpleyaml.exceptions.InvalidConfigurationException, IllegalArgumentException, IOException {
		getLogger().info(
				"Checking " + file.getConfigurationFile().getPath() + " for completeness (comparing to " + src + ")");
		de.ancash.misc.io.FileUtils.setMissingConfigurationSections(file, getResource(src),
				new HashSet<>(Arrays.asList("XMaterial")));
	}

	@Override
	public void onDisable() {
		if (iua != null) {
			protocolManager.removePacketListeners(this);
			iua.stop();
			iua = null;
		}
		HandlerList.unregisterAll(singleton);
	}

	public static NBTNexus getInstance() {
		return singleton;
	}
}
