package ru.practicum.shareit.booking;

import lombok.Data;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Service
@Data
public class BookingMapper {
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingDto fromBookingToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBookerId(booking.getBookerId());
        bookingDto.setBooker(userMapper.fromUserToDto(booking.getBooker()));
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setState(booking.getState());
        bookingDto.setItemId(booking.getItemId());
        bookingDto.setItem(itemMapper.fromItemToDto(booking.getItem()));
        bookingDto.setItemOwnerId(booking.getItemOwnerId());
        return bookingDto;
    }

    public Booking fromDtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItemId(bookingDto.getItemId());
        return booking;
    }
}
