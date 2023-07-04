package de.ancash.nbtnexus.serde.access;

import static de.ancash.nbtnexus.MetaTag.*;

import java.util.List;

import de.ancash.nbtnexus.serde.SerializedItem;

public class UnspecificMetaAccess extends SerializedMetaAccess {

	public UnspecificMetaAccess() {
		super(UNSPECIFIC_META_TAG);
	}

	@SuppressWarnings("nls")
	public String getDisplayName(SerializedItem si) {
		return si.getString(String.join(".", UNSPECIFIC_META_TAG, DISPLAYNAME_TAG));
	}

	@SuppressWarnings("nls")
	public String getLocalizedName(SerializedItem si) {
		return si.getString(String.join(".", UNSPECIFIC_META_TAG, LOCALIZED_NAME_TAG));
	}

	@SuppressWarnings("nls")
	public int getCustomModelData(SerializedItem si) {
		return si.getInt(String.join(".", UNSPECIFIC_META_TAG, CUSTOM_MODEL_DATA));
	}

	@SuppressWarnings("nls")
	public List<String> getLore(SerializedItem si) {
		return si.getList(String.join(".", UNSPECIFIC_META_TAG, LORE_TAG));
	}
}