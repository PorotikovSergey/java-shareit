package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    @Autowired
    public RequestController(RequestServiceImpl itemRequestService, RequestMapper requestMapper) {
        this.requestService = itemRequestService;
        this.requestMapper = requestMapper;
    }

    @PostMapping
    public RequestDto postItemRequest(HttpServletRequest requestor, @RequestBody RequestDto requestDto) {
        return requestMapper.fromRequestToDto(requestService.postRequest(requestMapper.fromDtoToRequest(requestDto),
                requestor.getHeader(USER_ID_HEADER)));
    }

    @GetMapping
    public List<RequestDto> getAll(HttpServletRequest requestor) {
        return requestService.getAll(requestor.getHeader(USER_ID_HEADER)).stream()
                .map(requestMapper::fromRequestToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemRequestId}")
    public RequestDto getItemRequest(HttpServletRequest requestor, @PathVariable String itemRequestId) {
        return requestMapper.fromRequestToDto(requestService.getRequest(itemRequestId,
                requestor.getHeader(USER_ID_HEADER)));
    }

    @GetMapping("/all")
    public List<RequestDto> getAllPageable(HttpServletRequest requestor,
                                           @RequestParam(required = false) String from,
                                           @RequestParam(required = false) String size) {
        return requestService.getAllPageable(from, size, requestor.getHeader(USER_ID_HEADER)).stream()
                .map(requestMapper::fromRequestToDto)
                .collect(Collectors.toList());
    }
}