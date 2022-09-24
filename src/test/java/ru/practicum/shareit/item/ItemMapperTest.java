package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {
    Mapper mapper = new Mapper();

    User requestor1 = new User(1L, "bob", "bob@mail.ru");

    Item item = new Item(1L, "item-1", "description-1", true,
            17, requestor1, new ArrayList<>(), null, null);

    ItemDto itemDto = new ItemDto(23L, "item-23", "description-23", true,
            17, null, null, new ArrayList<>(), 123);

    @Test
    void fromItemToDto() {
        ItemDto newDto = mapper.fromItemToDto(item);

        assertNotNull(newDto);
        assertEquals(1L, newDto.getId());
        assertEquals("item-1", newDto.getName());
        assertEquals("description-1", newDto.getDescription());
        assertEquals(17, newDto.getRequestId());
        assertNull(newDto.getLastBooking());
        assertNull(newDto.getNextBooking());
        assertEquals(0, newDto.getComments().size());
    }

    @Test
    void fromDtoToItem() {
        Item newItem = mapper.fromDtoToItem(itemDto);

        assertNotNull(newItem);
        assertEquals("item-23", newItem.getName());
        assertEquals("description-23", newItem.getDescription());
        assertTrue(newItem.getAvailable());
    }

}