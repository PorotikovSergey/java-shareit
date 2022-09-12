package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public Comment(long id, long itemId, long bookerId, String text) {
        this.id = id;
        this.itemId = itemId;
        this.bookerId = bookerId;
        this.text = text;
    }
}
