package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemServiceImpl itemService;
    private final Mapper mapper;

    @Autowired
    public ItemController(ItemServiceImpl itemService, Mapper mapper) {
        this.itemService = itemService;
        this.mapper = mapper;
    }

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader(USER_ID_HEADER) long userId,
                                      @RequestParam(name = "from", defaultValue = "0") Integer from,
                                      @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.getAll(userId, from, size).stream()
                .map(mapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto postItem(@RequestHeader(USER_ID_HEADER) long userId, @RequestBody ItemDto itemDto) {
        if (itemDto.getRequestId() == 0) {
            return mapper.fromItemToDto(itemService.postItem(mapper.fromDtoToItem(itemDto),
                    userId));
        }
        return mapper.fromItemToDto(itemService.postItemToRequest(mapper.fromDtoToItem(itemDto),
                userId, itemDto.getRequestId()));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader(USER_ID_HEADER) long userId,
                             @PathVariable long itemId,
                             @RequestBody ItemPatchDto itemPatchDto) {
        return mapper.fromItemToDto(itemService.patchItem(itemId, mapper.fromPatchDtoToItem(itemPatchDto),
                userId));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable long itemId) {
        return mapper.fromItemToDto(itemService.getItem(userId, itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestHeader(USER_ID_HEADER) long userId,
                                          @RequestParam String text,
                                          @RequestParam(name = "from", defaultValue = "0") Integer from,
                                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.searchItem(text, userId, from, size).stream()
                .map(mapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("{itemId}/comment")
    public CommentDto postComment(@RequestHeader(USER_ID_HEADER) long userId,
                                  @PathVariable long itemId,
                                  @RequestBody CommentDto commentDto) {
        return mapper.fromCommentToDto(itemService.postComment(userId,
                itemId, mapper.fromDtoToComment(commentDto)));
    }
}
