package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class CommentDto {
    private long id;
    private String text;
    private String authorName;
}
