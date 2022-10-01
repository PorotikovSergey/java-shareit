package ru.practicum.shareit.mapper.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.ItemDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    private long id;
    private String description;
    private long requestor;
    private Date created;
    private List<ItemDto> items = new ArrayList<>();
}
