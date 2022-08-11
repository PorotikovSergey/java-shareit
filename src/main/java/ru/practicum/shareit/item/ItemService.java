package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class ItemService {
    private final ItemMapper itemMapper;
    private final ItemStorage itemStorage;

    @Autowired
    public ItemService(ItemMapper itemMapper, ItemStorage itemStorage) {
        this.itemMapper = itemMapper;
        this.itemStorage = itemStorage;
    }

    public Collection<ItemDto> getAll(String ownerId) {
        Collection<ItemDto> resultCollection = new ArrayList<>();
        for(Item item: itemStorage.getAll(ownerId)) {
            resultCollection.add(itemMapper.fromItemToDto(item));
        }
        return resultCollection;
    }

    public ItemDto postItem(Item item, String ownerId) {
        return itemMapper.fromItemToDto(itemStorage.addItem(item, ownerId));
    }

    public void deleteItem(long itemId) {
        itemStorage.deleteItem(itemId);
    }

    public ItemDto patchItem(long itemId, Item item, String ownerId) {
        return itemMapper.fromItemToDto(itemStorage.patchItem(itemId, item, ownerId));
    }

    public ItemDto getItem(long itemId) {
        return itemMapper.fromItemToDto(itemStorage.getItem(itemId));
    }

    public Collection<ItemDto> searchItem(String text) {
        Collection<ItemDto> resultCollection = new ArrayList<>();
        for(Item item: itemStorage.searchItem(text)) {
            resultCollection.add(itemMapper.fromItemToDto(item));
        }
        return resultCollection;
    }
}
