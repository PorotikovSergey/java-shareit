package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "itemname")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "request_id")
    private long requestId;


    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)

    private User owner;

    @Transient
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Comment> comments = new ArrayList<>();

    @Transient

    private Booking lastBooking;

    @Transient

    private Booking nextBooking;
}
