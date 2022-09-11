package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {
    Collection<Item> getAll(String owner);

    Item postItem(Item item, String owner);

    void deleteItem(long itemId);

    Item patchItem(long itemId, Item item, String owner);

    Item getItem(String user, long itemId);

    Collection<Item> searchItem(String text, String owner);

    Comment postComment(String booker, long itemId, Comment comment);

    Item postItemToRequest(Item item, String itemOwner, long requestId);

    Collection<Item> getAllPageable(String owner, String from, String size);

    Collection<Item> searchItemPageable(String text, String owner, String from, String size);
}