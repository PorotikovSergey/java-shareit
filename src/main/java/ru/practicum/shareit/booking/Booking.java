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
    private LocalDateTime finish;

    @Column(name = "itemId")
    private long item;

    @Column(name = "bookerId")
    private long booker;

    @Column(name = "status")
    private BookingStatus status;
}