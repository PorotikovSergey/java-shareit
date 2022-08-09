package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Slf4j
@Service
public class ItemService {
    private final ItemStorage itemStorage;

    @Autowired
    public ItemService(ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    public Collection<Item> getAll() {
        return itemStorage.getAll();
    }

    public Item postItem(Item item) {
        return itemStorage.addItem(item);
    }

    public void deleteItem(long itemId) {
        itemStorage.deleteItem(itemId);
    }

    public Item patchItem(long itemId, Item item) {
        return itemStorage.patchItem(itemId, item);
    }

    public Item getItem(long itemId) {
        return itemStorage.getItem(itemId);
    }
}
