package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

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

    @Transient
    @ManyToOne
    private User booker;

    @Transient
    @OneToOne
    private Item item = new Item();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

}