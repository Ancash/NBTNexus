package de.ancash.nbtnexus.editor.validator;

import java.util.Optional;

import de.ancash.minecraft.cryptomorin.xseries.XMaterial;
import de.ancash.minecraft.inventory.editor.yml.AbstractInputValidator;
import de.ancash.minecraft.inventory.editor.yml.StringEditor;
import de.ancash.minecraft.inventory.editor.yml.ValueEditor;
import de.ancash.nbtnexus.MetaTag;

public class MaterialValidator extends AbstractInputValidator<String> {

	@Override
	public boolean isOfInterest(ValueEditor<?> arg0) {
		return arg0 instanceof StringEditor && arg0.hasKey() && MetaTag.XMATERIAL_TAG.equals(arg0.getKey())
				&& ValidatorUtil.isItemProperty(arg0, 1);
	}

	@SuppressWarnings("nls")
	@Override
	public Optional<String> isValid(ValueEditor<String> arg0, String arg1) {
		return isMaterial(arg1) ? Optional.empty() : Optional.of("invalid material");
	}

	protected boolean isMaterial(String m) {
		return XMaterial.matchXMaterial(m).isPresent();
	}
}
