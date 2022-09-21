package ru.practicum.shareit.booking;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class BookingMapper {

    public BookingDto fromBookingToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBooker(booking.getBooker());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setItem(booking.getItem());
        return bookingDto;
    }

    public Booking fromDtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        return booking;
    }
}
