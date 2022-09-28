package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.mapper.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingServiceImpl bookingService;
    private final Mapper mapper;

    @Autowired
    public BookingController(BookingServiceImpl bookingService, Mapper mapper) {
        this.bookingService = bookingService;
        this.mapper = mapper;
    }

    @PostMapping
    public BookingDto postBooking(@RequestHeader(USER_ID_HEADER) long userId, @RequestBody BookingDto bookingDto) {
        return mapper.fromBookingToDto(bookingService.postBooking(mapper.fromDtoToBooking(bookingDto),
                userId, bookingDto.getItemId()));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable Long bookingId,
                                   @RequestParam String approved) {

        return mapper.fromBookingToDto(bookingService.patchBooking(bookingId,
                userId, Boolean.parseBoolean(approved)));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(USER_ID_HEADER) long userId, @PathVariable Long bookingId) {
        return mapper.fromBookingToDto(bookingService.getBooking(userId, bookingId));
    }

    @GetMapping
    public List<BookingDto> getAll(@RequestHeader(USER_ID_HEADER) long userId,
                                   @RequestParam(required = false) String state,
                                   @RequestParam(name = "from", defaultValue = "0")
                                   Integer from,
                                   @RequestParam(name = "size", defaultValue = "1")
                                   Integer size) {
        return bookingService.getAllForBooker(state, from, size, userId).stream()
                .map(mapper::fromBookingToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getForCurrentUser(@RequestHeader(USER_ID_HEADER) long userId,
                                              @RequestParam(required = false) String state,
                                              @RequestParam(name = "from", defaultValue = "0")
                                              Integer from,
                                              @RequestParam(name = "size", defaultValue = "10")
                                              Integer size) {
        return bookingService.getAllForOwner(state, from, size, userId).stream()
                .map(mapper::fromBookingToDto)
                .collect(Collectors.toList());
    }
}
