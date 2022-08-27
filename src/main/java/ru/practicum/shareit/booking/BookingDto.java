package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime finish;
    private long itemId;
    private long bookerId;
    private User booker;
    private BookingStatus status;
}