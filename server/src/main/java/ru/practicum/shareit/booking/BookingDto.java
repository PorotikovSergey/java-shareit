package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private long itemId;
    private long bookerId;
    private UserDto booker;
    private ItemDto item;
    private BookingStatus status;
}
