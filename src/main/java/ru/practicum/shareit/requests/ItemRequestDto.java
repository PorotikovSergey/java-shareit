package ru.practicum.shareit.requests;

import java.time.LocalDateTime;

public class ItemRequestDto {
    private long id;
    private String description;
    private long requestor;
    private LocalDateTime createDate;
}