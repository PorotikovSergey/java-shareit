package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.BookingDto;

import java.util.List;

@Data
public class ItemDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
    private BookingDto lastBooking;
    private BookingDto nextBooking;
    private List<CommentDto> comments;
    private long requestId;
}