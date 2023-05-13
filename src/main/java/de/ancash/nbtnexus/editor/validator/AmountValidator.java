package de.ancash.nbtnexus.editor.validator;

import java.util.Optional;

import de.ancash.minecraft.inventory.editor.yml.AbstractInputValidator;
import de.ancash.minecraft.inventory.editor.yml.LongEditor;
import de.ancash.minecraft.inventory.editor.yml.ValueEditor;
import de.ancash.nbtnexus.MetaTag;

public class AmountValidator extends AbstractInputValidator<Long> {

	@Override
	public boolean isOfInterest(ValueEditor<?> arg0) {
		return arg0 instanceof LongEditor && arg0.hasKey() && MetaTag.AMOUNT_TAG.equals(arg0.getKey())
				&& ValidatorUtil.isItemProperty(arg0, 1);
	}

	@SuppressWarnings("nls")
	@Override
	public Optional<String> isValid(ValueEditor<Long> arg0, Long arg1) {
		if (arg1 <= 0 || arg1 > 64)
			return Optional.of("amount only 1-64");
		return Optional.empty();
	}

}
