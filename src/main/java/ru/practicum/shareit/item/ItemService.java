package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserStorage;

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

    public Item postItem(Item item, String ownerId) {
        return itemStorage.addItem(item, ownerId);
    }

    public void deleteItem(long itemId) {
        itemStorage.deleteItem(itemId);
    }

    public Item patchItem(long itemId, Item item, String ownerId) {
        return itemStorage.patchItem(itemId, item, ownerId);
    }

    public Item getItem(long itemId) {
        return itemStorage.getItem(itemId);
    }

    public Collection<Item> searchItem(String text) {
        return itemStorage.searchItem(text);
    }
}
