package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.practicum.shareit.user.User;

import javax.persistence.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

//    @ManyToOne
//    @JoinColumn(name = "booker_id", referencedColumnName = "id")
//    private User booker;


    @Column(name = "author_name")
    private String authorName;

    @Column(name = "text")
    private String text;

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", item=" + item +
                ", authorName='" + authorName + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
