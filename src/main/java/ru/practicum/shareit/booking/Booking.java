package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start")
    private LocalDateTime start;

    @Column(name = "finish")
    private LocalDateTime end;

    @Column(name = "item_id")
    private long itemId;

    @Column(name = "booker_id")
    private long bookerId;

    @Column(name = "owner_id")
    private long itemOwnerId;

    @Transient
    private UserDto booker;

    @Transient
    private ItemDto item;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private BookingState state = BookingState.ALL;
}