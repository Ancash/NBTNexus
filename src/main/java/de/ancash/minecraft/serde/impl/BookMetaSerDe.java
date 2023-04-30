package de.ancash.minecraft.serde.impl;

import static de.ancash.minecraft.serde.IItemTags.BOOK_AUTHOR_TAG;
import static de.ancash.minecraft.serde.IItemTags.BOOK_PAGES_TAG;
import static de.ancash.minecraft.serde.IItemTags.BOOK_TAG;
import static de.ancash.minecraft.serde.IItemTags.BOOK_TITLE_TAG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookMetaSerDe implements IItemSerializer, IItemDeserializer {

	public static final BookMetaSerDe INSTANCE = new BookMetaSerDe();

	BookMetaSerDe() {
	}

	@Override
	public Map<String, Object> serialize(ItemStack item) {
		Map<String, Object> map = new HashMap<>();
		BookMeta meta = (BookMeta) item.getItemMeta();
		if (!(meta.hasPages() || meta.hasAuthor() || meta.hasTitle()))
			return map;
		if (meta.hasAuthor()) {
			map.put(BOOK_AUTHOR_TAG, meta.getAuthor());
			meta.setAuthor(null);
		}
		if (meta.hasPages()) {
			map.put(BOOK_PAGES_TAG, meta.getPages());
			meta.setPages(new ArrayList<>());
		}
		if (meta.hasTitle()) {
			map.put(BOOK_TITLE_TAG, meta.getTitle());
			meta.setTitle(null);
		}
		item.setItemMeta(meta);
		return map;
	}

	@Override
	public boolean isValid(ItemStack item) {
		return item.getItemMeta() instanceof BookMeta;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ItemStack item, Map<String, Object> map) {
		BookMeta bm = (BookMeta) item.getItemMeta();
		bm.setAuthor((String) map.get(BOOK_AUTHOR_TAG));
		bm.setTitle((String) map.get(BOOK_TITLE_TAG));
		if (map.containsKey(BOOK_PAGES_TAG))
			bm.setPages((List<String>) map.get(BOOK_PAGES_TAG));
		item.setItemMeta(bm);
	}

	@Override
	public String getKey() {
		return BOOK_TAG;
	}
}
