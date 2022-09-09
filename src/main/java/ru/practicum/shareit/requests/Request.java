package ru.practicum.shareit.requests;

import lombok.Data;
import ru.practicum.shareit.item.Item;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "requests")
@Data
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "description")
    private String description;

    @Column(name = "requestor_id")
    private long requestor;

    @Column(name = "create_date")
    private Date created;

    @Transient
    private List<Item> items = new ArrayList<>();
}