package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * // TODO .
 */
@Data
@NoArgsConstructor
public class Item {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
}
