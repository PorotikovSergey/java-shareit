package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking postBooking(Booking booking, long userId, long itemId);

    Booking patchBooking(long bookingId, long userId, boolean approved);

    Booking getBooking(long userId, long bookingId);

    List<Booking> getAllForBooker(String state, int first, int size, long bookerId);

    List<Booking> getAllForOwner(String state, int first, int size, long userId);
}
