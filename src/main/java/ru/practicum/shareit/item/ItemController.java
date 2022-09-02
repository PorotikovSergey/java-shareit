package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {
    private static final String SHARER_ID_HEADER = "X-Sharer-User-Id";
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
    public Collection<ItemDto> getAll(HttpServletRequest request) {
        return itemService.getAll(request.getHeader(SHARER_ID_HEADER)).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ItemDto postItem(HttpServletRequest request, @RequestBody Item item) {
        return itemMapper.fromItemToDto(itemService.postItem(item, request.getHeader(SHARER_ID_HEADER)));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(HttpServletRequest request, @PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(HttpServletRequest request, @PathVariable long itemId, @RequestBody Item item) {
        return itemMapper.fromItemToDto(itemService.patchItem(itemId, item, request.getHeader(SHARER_ID_HEADER)));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(HttpServletRequest request, @PathVariable long itemId) {
        return itemMapper.fromItemToDto(itemService.getItem(request.getHeader(SHARER_ID_HEADER), itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(HttpServletRequest request, @RequestParam String text) {
        return itemService.searchItem(text, request.getHeader(SHARER_ID_HEADER)).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }

    @PostMapping("{itemId}/comment")
    public CommentDto postComment(HttpServletRequest request, @PathVariable long itemId, @RequestBody Comment comment) {
        return commentMapper.fromCommentToDto(itemService.postComment(request.getHeader(SHARER_ID_HEADER), itemId, comment));
    }
}