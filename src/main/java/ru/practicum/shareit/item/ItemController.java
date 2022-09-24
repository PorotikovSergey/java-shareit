package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final Mapper mapper;

    @Autowired
    public ItemController(ItemServiceImpl itemService, Mapper mapper) {
        this.itemService = itemService;
        this.mapper = mapper;
    }

    @GetMapping
    public Collection<ItemDto> getAll(HttpServletRequest request,
                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.getAll(request.getHeader(USER_ID_HEADER), from, size).stream()
                .map(mapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto postItem(HttpServletRequest request, @Valid @RequestBody ItemDto itemDto) {
        if (itemDto.getRequestId() == 0) {
            return mapper.fromItemToDto(itemService.postItem(mapper.fromDtoToItem(itemDto),
                    request.getHeader(USER_ID_HEADER)));
        }
        return mapper.fromItemToDto(itemService.postItemToRequest(mapper.fromDtoToItem(itemDto),
                request.getHeader(USER_ID_HEADER), itemDto.getRequestId()));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(HttpServletRequest request, @PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(HttpServletRequest request,
                             @PathVariable long itemId,
                             @RequestBody ItemDto itemDto) {
        return mapper.fromItemToDto(itemService.patchItem(itemId, mapper.fromDtoToItem(itemDto),
                request.getHeader(USER_ID_HEADER)));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(HttpServletRequest request, @PathVariable long itemId) {
        return mapper.fromItemToDto(itemService.getItem(request.getHeader(USER_ID_HEADER), itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(HttpServletRequest request,
                                          @RequestParam String text,
                                          @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                          @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.searchItem(text, request.getHeader(USER_ID_HEADER), from, size).stream()
                .map(mapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("{itemId}/comment")
    public CommentDto postComment(HttpServletRequest request,
                                  @PathVariable long itemId,
                                  @Valid @RequestBody CommentDto commentDto) {

        return mapper.fromCommentToDto(itemService.postComment(request.getHeader(USER_ID_HEADER),
                itemId, mapper.fromDtoToComment(commentDto)));
    }
}