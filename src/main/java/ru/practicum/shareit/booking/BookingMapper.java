package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;

@Service
public class BookingMapper {
    public BookingDto fromBookingToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBookerId(booking.getBookerId());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setItemId(booking.getItemId());
        bookingDto.setBooker(booking.getBooker());
        bookingDto.setItem(booking.getItem());
        return bookingDto;
    }

    public Booking fromDtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setBookerId(bookingDto.getBookerId());
        booking.setStatus(bookingDto.getStatus());
        booking.setItemId(bookingDto.getItemId());
        booking.setBooker(bookingDto.getBooker());
        booking.setItem(bookingDto.getItem());
        return booking;
    }
}