package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestServiceImpl itemRequestService) {
        this.requestService = itemRequestService;
    }

    @PostMapping
    public Request postItemRequest(HttpServletRequest requestor, @RequestBody Request request) {
        return requestService.postRequest(request, requestor.getHeader(USER_ID_HEADER));
    }

    @GetMapping
    public Collection<Request> getAll(HttpServletRequest requestor) {
        return requestService.getAll(requestor.getHeader(USER_ID_HEADER));
    }

    @GetMapping("/{itemRequestId}")
    public String getItemRequest(@PathVariable String itemRequestId) {
        return itemRequestId;
    }

    @GetMapping("/all")
    public Collection<Request> getAllPageable(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String size) {
        return requestService.getAllPageable(from, size);
    }
}