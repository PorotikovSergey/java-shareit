package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    BookingMapper mapper = new BookingMapper();

    User booker = new User(1L, "Bob", "pochta@hotmail.com");
    Item item = new Item(1L, "item-1", "description-1", true,
            17, booker, new ArrayList<>(), null, null);

    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(12);

    Booking booking = new Booking(1L, start, end, booker, item, BookingStatus.WAITING, BookingState.ALL);
    BookingDto bookingDto = new BookingDto(23L, start, end, 1L, 1L,
            booker, item, BookingStatus.WAITING, BookingState.ALL);

    @Test
    void fromBookingToDto() {
        BookingDto newDto = mapper.fromBookingToDto(booking);

        assertNotNull(newDto);
        assertEquals(1L, newDto.getId());
        assertEquals(start, newDto.getStart());
        assertEquals(end, newDto.getEnd());
        assertEquals(booker, newDto.getBooker());
        assertEquals(item, newDto.getItem());
        assertEquals(BookingStatus.WAITING, newDto.getStatus());
        assertEquals(BookingState.ALL, newDto.getState());
    }

    @Test
    void fromDtoToBooking() {
        Booking newBooking = mapper.fromDtoToBooking(bookingDto);

        assertNotNull(newBooking);
        assertEquals(start, newBooking.getStart());
        assertEquals(end, newBooking.getEnd());
    }
}