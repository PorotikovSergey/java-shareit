package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
@Qualifier("itemStorage")
public class ItemStorage {
    private Map<Long, Item> items = new HashMap();

    public Collection<Item> getAll() {
        return items.values();
    }

    public Item addItem(Item item) {
        item.setId(ItemIdManager.getItemId());
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

    public Collection<Item> searchItem(String text) {
        Collection<Item> resultList = new ArrayList<>();
        Collection<Item> itemList = items.values();
        for (Item item: itemList) {
            if(checkTextInDescriptionAndName(item, text)) {
                resultList.add(item);
            }
        }
        return resultList;
    }

//==================================================================

    private boolean checkTextInDescriptionAndName(Item item, String text) {
        String checkText = text.toLowerCase();
        String description = item.getDescription().toLowerCase();
        String name = item.getName().toLowerCase();
        return description.contains(checkText) || name.contains(checkText);
    }
}
