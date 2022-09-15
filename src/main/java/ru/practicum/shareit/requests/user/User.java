package ru.practicum.shareit.requests.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.requests.Request;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "owner")
    private List<Item> items;

    @OneToMany(mappedBy = "requestor")
    private List<Request> requests;

    @OneToMany(mappedBy = "booker")
    @JsonIgnore
    private List<Booking> bookings;
}