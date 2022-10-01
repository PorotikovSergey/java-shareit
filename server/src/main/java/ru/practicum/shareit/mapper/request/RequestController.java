package ru.practicum.shareit.mapper.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestServiceImpl requestService;
    private final Mapper mapper;

    @Autowired
    public RequestController(RequestServiceImpl itemRequestService, Mapper mapper) {
        this.requestService = itemRequestService;
        this.mapper = mapper;
    }

    @PostMapping
    public RequestDto postItemRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                      @RequestBody RequestDto requestDto) {
        return mapper.fromRequestToDto(requestService.postRequest(mapper.fromDtoToRequest(requestDto), userId));
    }

    @GetMapping
    public List<RequestDto> getAll(@RequestHeader(USER_ID_HEADER) long userId) {
        return requestService.getAll(userId).stream()
                .map(mapper::fromRequestToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemRequestId}")
    public RequestDto getItemRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                     @PathVariable String itemRequestId) {
        return mapper.fromRequestToDto(requestService.getRequest(itemRequestId,
                userId));
    }

    @GetMapping("/all")
    public List<RequestDto> getAllPageable(@RequestHeader(USER_ID_HEADER) long userId,
                                           @RequestParam(name = "from", defaultValue = "0")
                                           Integer from,
                                           @RequestParam(name = "size", defaultValue = "10")
                                           Integer size) {
        return requestService.getAllPageable(from, size, userId).stream()
                .map(mapper::fromRequestToDto)
                .collect(Collectors.toList());
    }
}
