package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

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
    public Collection<Item> getAll() {
        return itemService.getAll();
    }

    @PostMapping
    public Item postItem(HttpServletRequest request, @RequestBody Item item) {
        return itemService.postItem(item, request.getHeader("X-Sharer-User-Id"));
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(HttpServletRequest request, @PathVariable long itemId) {
        itemService.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public Item patchItem(HttpServletRequest request, @PathVariable long itemId, @RequestBody Item item) {
        return itemService.patchItem(itemId, item, request.getHeader("X-Sharer-User-Id"));
    }

    @GetMapping("/{itemId}")
    public Item getItem(@PathVariable long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping("/search?text={text}")
    public Collection<Item> searchItem(@RequestParam String text) {
        return itemService.searchItem(text);
    }
}
