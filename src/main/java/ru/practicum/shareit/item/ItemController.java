package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemController(ItemServiceImpl itemService, ItemMapper itemMapper, CommentMapper commentMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping
    public Collection<ItemDto> getAll(HttpServletRequest request,
                                      @RequestParam(required = false) String from,
                                      @RequestParam(required = false) String size) {

        return itemService.getAll(request.getHeader(USER_ID_HEADER), from, size).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto postItem(HttpServletRequest request, @RequestBody ItemDto itemDto) {
        if (itemDto.getRequestId() == 0) {
            return itemMapper.fromItemToDto(itemService.postItem(itemMapper.fromDtoToItem(itemDto),
                    request.getHeader(USER_ID_HEADER)));
        }
        return itemMapper.fromItemToDto(itemService.postItemToRequest(itemMapper.fromDtoToItem(itemDto),
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

        return itemMapper.fromItemToDto(itemService.patchItem(itemId, itemMapper.fromDtoToItem(itemDto),
                request.getHeader(USER_ID_HEADER)));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(HttpServletRequest request, @PathVariable long itemId) {
        return itemMapper.fromItemToDto(itemService.getItem(request.getHeader(USER_ID_HEADER), itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(HttpServletRequest request,
                                          @RequestParam String text,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String size) {

        return itemService.searchItem(text, request.getHeader(USER_ID_HEADER), from, size).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("{itemId}/comment")
    public CommentDto postComment(HttpServletRequest request,
                                  @PathVariable long itemId,
                                  @RequestBody CommentDto commentDto) {

        return commentMapper.fromCommentToDto(itemService.postComment(request.getHeader(USER_ID_HEADER),
                itemId, commentMapper.fromDtoToComment(commentDto)));
    }
}