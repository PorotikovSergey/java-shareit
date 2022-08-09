package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@Qualifier("itemStorage")
public class ItemStorage {
    private Map<Long, Item> items = new HashMap();

    public Collection<Item> getAll() {
        return items.values();
    }

    public Item addItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Item getItem(long id) {
        return items.get(id);
    }

    public void deleteItem(long id) {
        items.remove(id);
    }

    public Item patchItem(long id, Item newItem) {
        return items.replace(id, newItem);
    }
}
