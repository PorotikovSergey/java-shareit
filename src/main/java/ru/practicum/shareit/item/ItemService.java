package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {
    Collection<Item> getAll(String ownerId);

    Item postItem(Item item, String ownerId);

    void deleteItem(long itemId);

    Item patchItem(long itemId, Item item, String ownerId);

    Item getItem(String user, long itemId);

    Collection<Item> searchItem(String text, String ownerId);
}