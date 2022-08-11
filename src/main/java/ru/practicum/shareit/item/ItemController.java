package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * // TODO .
 */
@RestController
@RequestMapping("/items")
public class ItemController {              //X-Sharer-User-Id
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public Collection<ItemDto> getAll(HttpServletRequest request) {
        return itemService.getAll(request.getHeader("X-Sharer-User-Id"));
    }

    @PostMapping
    public ItemDto postItem(HttpServletRequest request, @RequestBody Item item) {
        return itemService.postItem(item, request.getHeader("X-Sharer-User-Id"));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(HttpServletRequest request, @PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(HttpServletRequest request, @PathVariable long itemId, @RequestBody Item item){
            return itemService.patchItem(itemId, item, request.getHeader("X-Sharer-User-Id"));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItem(@RequestParam String text) {
        return itemService.searchItem(text);
    }
}
