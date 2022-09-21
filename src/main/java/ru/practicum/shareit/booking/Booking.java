package ru.practicum.shareit.booking;

import lombok.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start")
    private LocalDateTime start;

    @Column(name = "finish")
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "booker_id", referencedColumnName = "id")
    private User booker;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.WAITING;

    public Booking(long id, LocalDateTime start, LocalDateTime end, User booker, Item item) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.booker = booker;
        this.item = item;
    }
}