package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@Validated
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestClient requestClient;

    @Autowired
    public RequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> postItemRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                                  @RequestBody @Valid RequestDto requestDto) {
        log.info("Post request with userId={}, dto={}", userId, requestDto);
        return requestClient.postRequest(requestDto,
                userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get all requests with userId={}", userId);
        return requestClient.getAll(userId);
    }

    @GetMapping("/{itemRequestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                                 @PathVariable String itemRequestId) {
        log.info("Get request with userId={}, itemRequestId={}", userId, itemRequestId);
        return requestClient.getRequest(itemRequestId,
                userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllPageable(@RequestHeader(USER_ID_HEADER) long userId,
                                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                 Integer from,
                                                 @Positive @RequestParam(name = "size", defaultValue = "10")
                                                 Integer size) {
        log.info("Get requests with userId={}, from={}, size={}", userId, from, size);
        return requestClient.getAllPageable(from, size, userId);
    }
}
