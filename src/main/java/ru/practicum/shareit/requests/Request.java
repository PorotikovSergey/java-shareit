package ru.practicum.shareit.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "description")
    private String description;

    @Column(name = "create_date")
    private Date created;

    @ManyToOne
    @JoinColumn(name = "requestor_id", referencedColumnName = "id")
    @JsonIgnore
    private User requestor;

    @Transient
    private List<Item> items = new ArrayList<>();

//    public Request(long id, String description, Date created, User requestor, List<Item> items) {
//        this.id = id;
//        this.description = description;
//        this.created = created;
//        this.requestor = requestor;
//        this.items = items;
//    }

}