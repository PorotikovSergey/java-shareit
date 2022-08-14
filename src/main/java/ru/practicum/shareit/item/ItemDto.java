package ru.practicum.shareit.item;

import lombok.Data;

@Data
public class ItemDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
}