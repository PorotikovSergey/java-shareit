package ru.practicum.shareit.requests;

import lombok.Data;
import ru.practicum.shareit.item.Item;

import java.util.Date;
import java.util.List;

@Data
public class RequestDto {
    private long id;
    private String description;
    private long requestor;
    private Date created;
    private List<Item> items;
}