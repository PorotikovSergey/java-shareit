package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class BookingDtoOutput {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private User booker;
    private Item item;
    private BookingStatus status;
    private BookingState state;

    @Override
    public String toString() {
        return "BookingDto{" +
                "id=" + id +
                ", start=" + start +
                ", end=" + end +
                ", booker=" + booker +
                ", item=" + item +
                ", status=" + status +
                ", state=" + state +
                '}';
    }
}
