package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;

@Service
public class ItemMapper {

    public ItemDto fromItemToDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwnerId());
        itemDto.setLastBooking(item.getLastBooking());
        itemDto.setNextBooking(item.getNextBooking());
        itemDto.setComments(item.getComments());
        itemDto.setRequestId(item.getRequestId());
        return itemDto;
    }

    public Item fromDtoToItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequestId(itemDto.getRequestId());
        return item;
    }
}