package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;

@Service
public class ItemMapper {

    public ItemDto fromItemToDto(Item item) {
        System.out.println("вошёл айтем ");
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setLastBooking(item.getLastBooking());
        itemDto.setNextBooking(item.getNextBooking());
        itemDto.setComments(item.getComments());
        itemDto.setRequestId(item.getRequestId());
        System.out.println("выходит дто ");
        return itemDto;
    }


    public Item fromDtoToItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
//        item.setRequestId(itemDto.getRequestId());
        return item;
    }
}