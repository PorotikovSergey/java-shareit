package ru.practicum.shareit.booking;

import lombok.Data;

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

    @Column(name = "item_Id")
    private long itemId;

    @Column(name = "booker_Id")
    private long bookerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;
}