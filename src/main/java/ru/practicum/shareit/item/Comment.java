package ru.practicum.shareit.item;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "item_id")
    private long itemId;

    @Column(name = "booker_id")
    private long bookerId;

    @Transient
    private String authorName;

    @Column(name = "text")
    private String text;
}
