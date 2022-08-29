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
        bookingDto.setBooker(booking.getBooker());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setState(booking.getState());
        bookingDto.setItemId(booking.getItemId());
        bookingDto.setItem(booking.getItem());
        bookingDto.setItemOwnerId(booking.getItemOwnerId());
        return bookingDto;
    }

    public Booking fromDtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setBookerId(bookingDto.getBookerId());
        booking.setBooker(bookingDto.getBooker());
        booking.setItemOwnerId(bookingDto.getItemOwnerId());
        booking.setItemId(bookingDto.getItemId());
        booking.setItem(bookingDto.getItem());
        booking.setStatus(bookingDto.getStatus());
        booking.setState(bookingDto.getState());
        return booking;
    }
}
