package ru.practicum.shareit.item.model;

import lombok.Data;

/**
 * // TODO .
 */
@Data
public class Item {
    private long id;
    private String name;
    private String description;
    private boolean available;
    private long ownerId = 0;

    public Item(String name, String description, boolean available) {
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
