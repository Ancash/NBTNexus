package de.ancash.nbtnexus.serde.access;

import static de.ancash.nbtnexus.MetaTag.*;
import static de.ancash.nbtnexus.serde.access.MapAccessUtil.*;

import java.util.Map;

public class DamageableMetaAccess extends SerializedMetaAccess {

	public DamageableMetaAccess() {
		super(DAMAGEABLE_TAG);
	}

	public int getDamage(Map<String, Object> map) {
		return getInt(map, joinPath(DAMAGEABLE_DAMAGE_TAG));
	}
}