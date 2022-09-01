package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String SHARER_ID_HEADER = "X-Sharer-User-Id";
    BookingService bookingService;
    BookingMapper bookingMapper;

    @Autowired
    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public BookingDto postBooking(HttpServletRequest request, @RequestBody Booking booking) {
        return bookingMapper.fromBookingToDto(bookingService.postBooking(booking, request.getHeader(SHARER_ID_HEADER)));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(HttpServletRequest request, @PathVariable String bookingId,
                                   @RequestParam String approved) {
        return bookingMapper.fromBookingToDto(bookingService.patchBooking(bookingId,
                request.getHeader(SHARER_ID_HEADER), approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(HttpServletRequest request, @PathVariable String bookingId) {
        return bookingMapper.fromBookingToDto(bookingService.getBooking(request.getHeader(SHARER_ID_HEADER), bookingId));
    }

    @GetMapping
    public Collection<BookingDto> getAll(HttpServletRequest request) {
            return bookingService.getAllByBooker(request.getQueryString(), request.getHeader(SHARER_ID_HEADER)).stream()
                    .map(bookingMapper::fromBookingToDto)
                    .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getForCurrentUser(HttpServletRequest request) {
        return bookingService.getAllForCurrentUser(request.getQueryString(), request.getHeader(SHARER_ID_HEADER)).stream()
                .map(bookingMapper::fromBookingToDto)
                .collect(Collectors.toList());
    }
}