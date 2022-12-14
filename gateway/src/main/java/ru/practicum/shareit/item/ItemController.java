package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPatchDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@Validated
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) long userId,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                         Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10")
                                         Integer size) {
        log.info("Get all items with userId={}, from={}, size={}", userId, from, size);
        return itemClient.getAll(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> postItem(@RequestHeader(USER_ID_HEADER) long userId,
                                           @RequestBody @Valid ItemDto itemDto) {
        log.info("Post item with userId={}, dto={}", userId, itemDto);
        if (itemDto.getRequestId() == 0) {
            return itemClient.postItem(itemDto,
                    userId);
        }
        return itemClient.postItemToRequest(itemDto,
                userId, itemDto.getRequestId());
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable long itemId) {
        log.info("Delete item with Id={}", itemId);
        itemClient.deleteItem(itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patchItem(@RequestHeader(USER_ID_HEADER) long userId,
                                            @PathVariable long itemId,
                                            @RequestBody @Valid ItemPatchDto itemPatchDto) {
        log.info("Patch item with userId={}, itemId={}, dto={}", userId, itemId, itemPatchDto);
        return itemClient.patchItem(itemId, itemPatchDto,
                userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable long itemId) {
        log.info("Get item with userId={}, itemId={}", userId, itemId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text,
                                             @RequestHeader(USER_ID_HEADER) long userId,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                             Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10")
                                             Integer size) {
        log.info("Search items with userId={}, from={}, size={}", userId, from, size);
        return itemClient.searchItem(text, userId, from, size);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader(USER_ID_HEADER) long userId,
                                              @PathVariable long itemId,
                                              @RequestBody @Valid CommentDto commentDto) {
        log.info("Post comment with userId={}, itemId={}, dto={}", userId, itemId, commentDto);
        return itemClient.postComment(userId,
                itemId, commentDto);
    }
}
