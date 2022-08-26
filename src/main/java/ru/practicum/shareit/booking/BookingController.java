package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
}