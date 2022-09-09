package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.Booking;

import java.util.List;

@Data
public class ItemDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<Comment> comments;
}