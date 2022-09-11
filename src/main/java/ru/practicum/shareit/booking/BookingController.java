package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public BookingDto postBooking(HttpServletRequest request, @RequestBody BookingDto bookingDto) {
        return bookingMapper.fromBookingToDto(bookingService.postBooking(bookingMapper.fromDtoToBooking(bookingDto), request.getHeader(USER_ID_HEADER)));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(HttpServletRequest request, @PathVariable String bookingId,
                                   @RequestParam String approved) {

        return bookingMapper.fromBookingToDto(bookingService.patchBooking(bookingId,
                request.getHeader(USER_ID_HEADER), Boolean.parseBoolean(approved)));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(HttpServletRequest request, @PathVariable String bookingId) {
        return bookingMapper.fromBookingToDto(bookingService.getBooking(request.getHeader(USER_ID_HEADER), bookingId));
    }

    @GetMapping
    public List<BookingDto> getAll(HttpServletRequest request,
                                   @RequestParam(required = false) String state,
                                   @RequestParam(required = false) String from,
                                   @RequestParam(required = false) String size) {
        return bookingService.getAllForBooker(state, from, size, request.getHeader(USER_ID_HEADER)).stream()
                .map(bookingMapper::fromBookingToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getForCurrentUser(HttpServletRequest request,
                                              @RequestParam(required = false) String state,
                                              @RequestParam(required = false) String from,
                                              @RequestParam(required = false) String size) {
        return bookingService.getAllForOwner(state, from, size, request.getHeader(USER_ID_HEADER)).stream()
                .map(bookingMapper::fromBookingToDto)
                .collect(Collectors.toList());
    }
}