package ru.practicum.shareit.item;

import lombok.Data;
import ru.practicum.shareit.booking.Booking;

import javax.persistence.*;

@Entity
@Table(name = "items")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "available")
    private Boolean available;

    @Column(name = "owner_Id")
    private long ownerId;

    @Transient
    private Booking lastBooking;

    @Transient
    private Booking nextBooking;
}