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

    @Autowired
    public ItemController(ItemServiceImpl itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
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
    public ItemDto patchItem(HttpServletRequest request, @PathVariable long itemId, @RequestBody Item item){
            return itemMapper.fromItemToDto(itemService.patchItem(itemId, item, request.getHeader(SHARER_ID_HEADER)));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        return itemMapper.fromItemToDto(itemService.getItem(itemId));
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam String text) {
        return itemService.searchItem(text).stream()
                .map(itemMapper::fromItemToDto)
                .collect(Collectors.toList());
    }
}