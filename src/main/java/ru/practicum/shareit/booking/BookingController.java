package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;
    private final Mapper mapper;

    @Autowired
    public BookingController(BookingService bookingService, Mapper mapper) {
        this.bookingService = bookingService;
        this.mapper = mapper;
    }

    @PostMapping
    public BookingDto postBooking(HttpServletRequest request, @Valid @RequestBody BookingDto bookingDto) {
        return mapper.fromBookingToDto(bookingService.postBooking(mapper.fromDtoToBooking(bookingDto),
                request.getHeader(USER_ID_HEADER), bookingDto.getItemId()));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(HttpServletRequest request, @PathVariable String bookingId,
                                   @RequestParam String approved) {

        return mapper.fromBookingToDto(bookingService.patchBooking(bookingId,
                request.getHeader(USER_ID_HEADER), Boolean.parseBoolean(approved)));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(HttpServletRequest request, @PathVariable String bookingId) {
        return mapper.fromBookingToDto(bookingService.getBooking(request.getHeader(USER_ID_HEADER), bookingId));
    }

    @GetMapping
    public List<BookingDto> getAll(HttpServletRequest request,
                                   @RequestParam(required = false) String state,
                                   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                   Integer from,
                                   @Positive @RequestParam(name = "size", defaultValue = "1")
                                   Integer size) {
        return bookingService.getAllForBooker(state, from, size, request.getHeader(USER_ID_HEADER)).stream()
                .map(mapper::fromBookingToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getForCurrentUser(HttpServletRequest request,
                                              @RequestParam(required = false) String state,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                              Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10")
                                              Integer size) {
        return bookingService.getAllForOwner(state, from, size, request.getHeader(USER_ID_HEADER)).stream()
                .map(mapper::fromBookingToDto)
                .collect(Collectors.toList());
    }
}