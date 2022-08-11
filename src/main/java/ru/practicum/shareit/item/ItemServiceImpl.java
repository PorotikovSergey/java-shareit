package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService{
    private final ItemMapper itemMapper;
    private final ItemStorage itemStorage;

    @Autowired
    public ItemServiceImpl(ItemMapper itemMapper, ItemStorage itemStorage) {
        this.itemMapper = itemMapper;
        this.itemStorage = itemStorage;
    }

    public Collection<ItemDto> getAll(String ownerId) {
        return itemStorage.getAll(ownerId).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
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
        return itemStorage.searchItem(text).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }
}
