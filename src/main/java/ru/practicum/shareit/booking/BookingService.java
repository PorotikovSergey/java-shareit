package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking postBooking(Booking booking, String bookerId, long itemId);

    Booking patchBooking(String bookingId, String sharerId, boolean approved);

    Booking getBooking(String ownerOrBooker, String bookingId);

    List<Booking> getAllForBooker(String state, int first, int size, String booker);

    List<Booking> getAllForOwner(String state, int first, int size, String user);
}
