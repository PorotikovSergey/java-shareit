package ru.practicum.shareit.requests;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "description")
    private String description;

    @Column(name = "requestor_id")
    private long requestor;

    @Column(name = "createDate")
    private LocalDateTime createDate;
}