package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    List<Item> getAll(long ownerId, int from, int size);

    Item postItem(Item item, long ownerId);

    void deleteItem(long itemId);

    Item patchItem(long itemId, Item item, long ownerId);

    Item getItem(long userId, long itemId);

    Collection<Item> searchItem(String text, long ownerId, int from, int size);

    Comment postComment(long bookerId, long itemId, Comment comment);

    Item postItemToRequest(Item item, long ownerId, long requestId);
}
