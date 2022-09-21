package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestService requestService;
    private final Mapper mapper;

    @Autowired
    public RequestController(RequestServiceImpl itemRequestService, Mapper mapper) {
        this.requestService = itemRequestService;
        this.mapper = mapper;
    }

    @PostMapping
    public RequestDto postItemRequest(HttpServletRequest requestor, @RequestBody RequestDto requestDto) {
        return mapper.fromRequestToDto(requestService.postRequest(mapper.fromDtoToRequest(requestDto),
                requestor.getHeader(USER_ID_HEADER)));
    }

    @GetMapping
    public List<RequestDto> getAll(HttpServletRequest requestor) {
        return requestService.getAll(requestor.getHeader(USER_ID_HEADER)).stream()
                .map(mapper::fromRequestToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemRequestId}")
    public RequestDto getItemRequest(HttpServletRequest requestor, @PathVariable String itemRequestId) {
        return mapper.fromRequestToDto(requestService.getRequest(itemRequestId,
                requestor.getHeader(USER_ID_HEADER)));
    }

    @GetMapping("/all")
    public List<RequestDto> getAllPageable(HttpServletRequest requestor,
                                           @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                           Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "1")
                                               Integer size) {
        return requestService.getAllPageable(from, size, requestor.getHeader(USER_ID_HEADER)).stream()
                .map(mapper::fromRequestToDto)
                .collect(Collectors.toList());
    }
}