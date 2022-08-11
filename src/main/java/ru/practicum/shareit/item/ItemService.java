package ru.practicum.shareit.item;

import java.util.Collection;

public interface ItemService {

    public Collection<ItemDto> getAll(String ownerId);

    public ItemDto postItem(Item item, String ownerId);

    public void deleteItem(long itemId);

    public ItemDto patchItem(long itemId, Item item, String ownerId);

    public ItemDto getItem(long itemId);

    public Collection<ItemDto> searchItem(String text);
}
