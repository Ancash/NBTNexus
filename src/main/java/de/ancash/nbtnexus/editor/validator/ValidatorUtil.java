package de.ancash.nbtnexus.editor.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.ancash.libs.org.simpleyaml.configuration.ConfigurationSection;
import de.ancash.minecraft.inventory.editor.yml.ConfigurationSectionEditor;
import de.ancash.minecraft.inventory.editor.yml.ValueEditor;
import de.ancash.nbtnexus.MetaTag;
import de.ancash.nbtnexus.serde.handler.AxolotlBucketMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BannerMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.BundleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.CompassMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkEffectMetaSerDe;
import de.ancash.nbtnexus.serde.handler.FireworkMetaSerDe;
import de.ancash.nbtnexus.serde.handler.KnowledgeBookMetaSerDe;
import de.ancash.nbtnexus.serde.handler.LeatherArmorMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MapMetaSerDe;
import de.ancash.nbtnexus.serde.handler.MusicInstrumentMetaSerDe;
import de.ancash.nbtnexus.serde.handler.PotionMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SimpleMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SkullMetaMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SpawnEggMetaSerDe;
import de.ancash.nbtnexus.serde.handler.SuspiciousStewMetaSerDe;
import de.ancash.nbtnexus.serde.handler.TropicalFishBucketMetaSerDe;

@SuppressWarnings("deprecation")
public class ValidatorUtil {

	private static final Set<String> metaTags = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(MetaTag.NBT_NEXUS_ITEM_PROPERTIES_TAG, AxolotlBucketMetaSerDe.INSTANCE.getKey(),
					BannerMetaSerDe.INSTANCE.getKey(), BookMetaSerDe.INSTANCE.getKey(),
					BundleMetaSerDe.INSTANCE.getKey(), CompassMetaSerDe.INSTANCE.getKey(),
					FireworkEffectMetaSerDe.INSTANCE.getKey(), FireworkMetaSerDe.INSTANCE.getKey(),
					KnowledgeBookMetaSerDe.INSTANCE.getKey(), LeatherArmorMetaSerDe.INSTANCE.getKey(),
					MapMetaSerDe.INSTANCE.getKey(), MusicInstrumentMetaSerDe.INSTANCE.getKey(),
					PotionMetaSerDe.INSTANCE.getKey(), SimpleMetaSerDe.INSTANCE.getKey(),
					SkullMetaMetaSerDe.INSTANCE.getKey(), SpawnEggMetaSerDe.INSTANCE.getKey(),
					SuspiciousStewMetaSerDe.INSTANCE.getKey(), TropicalFishBucketMetaSerDe.INSTANCE.getKey())));

	public static boolean isItemProperty(ValueEditor<?> cur, int depth) {
		if (depth > 0) {
			if (!cur.hasParent())
				return false;
			return isItemProperty(cur.getParent(), depth - 1);
		}

		return isItemRoot(cur);
	}

	public static boolean isItemProperty(ConfigurationSection cur, int depth) {
		if (depth > 0) {
			if (cur.getParent() == null)
				return false;
			return isItemProperty(cur.getParent(), depth - 1);
		}

		return isItemRoot(cur);
	}

	public static boolean isItemProperty(ValueEditor<?> cur) {
		if (!isItemRoot(cur)) {
			if (!cur.hasParent())
				return false;
			else
				return isItemProperty(cur.getParent());
		}
		return true;
	}

	public static boolean isItemProperty(ConfigurationSection cur) {
		if (!isItemRoot(cur)) {
			if (cur.getParent() == null)
				return false;
			else
				return isItemProperty(cur.getParent());
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static ValueEditor<ConfigurationSection> getItemRoot(ValueEditor<?> cur) {
		if (!isItemRoot(cur)) {
			if (!cur.hasParent())
				return null;
			else
				return getItemRoot(cur.getParent());
		}
		return (ValueEditor<ConfigurationSection>) cur;
	}

	public static ConfigurationSection getItemRoot(ConfigurationSection cur) {
		if (!isItemRoot(cur)) {
			if (cur.getParent() == null)
				return null;
			else
				return getItemRoot(cur.getParent());
		}
		return cur;
	}

	public static ValueEditor<?> getOneBeforeItemRoot(ValueEditor<?> cur) {
		if (!cur.hasParent())
			return null;

		if (isItemRoot(cur.getParent()))
			return (ValueEditor<?>) cur;

		return getOneBeforeItemRoot(cur.getParent());
	}

	public static ConfigurationSection getOneBeforeItemRoot(ConfigurationSection cur) {
		if (cur.getParent() == null)
			return null;

		if (isItemRoot(cur.getParent()))
			return cur;

		return getOneBeforeItemRoot(cur.getParent());
	}

	public static boolean isItemRoot(ValueEditor<?> cur) {
		if (!(cur instanceof ConfigurationSectionEditor))
			return false;
		return ((ConfigurationSectionEditor) cur).getCurrent()
				.isConfigurationSection(MetaTag.NBT_NEXUS_ITEM_PROPERTIES_TAG);
	}

	public static boolean isItemRoot(ConfigurationSection cur) {
		return cur.isConfigurationSection(MetaTag.NBT_NEXUS_ITEM_PROPERTIES_TAG);
	}

	public static boolean isMetaEditor(ValueEditor<?> c) {
		return metaTags.contains(c.getKey());
	}

	@SuppressWarnings("nls")
	public static boolean isMetaEditor(ConfigurationSection c) {
		if (c.getCurrentPath().isEmpty())
			return false;
		String[] path = c.getCurrentPath().split("\\.");
		return metaTags.contains(path[path.length - 1]);
	}

	public static boolean isMetaValue(ValueEditor<?> cur) {
		ValueEditor<?> oneBefor = getOneBeforeItemRoot(cur);
		if (oneBefor == null || !(oneBefor instanceof ConfigurationSectionEditor))
			return false;
		return isMetaEditor(oneBefor);
	}

	public static boolean isMetaValue(ConfigurationSection cur) {
		cur = getOneBeforeItemRoot(cur);
		return cur != null && isMetaEditor(cur);
	}
}
