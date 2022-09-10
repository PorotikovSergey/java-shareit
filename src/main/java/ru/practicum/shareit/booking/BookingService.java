package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {
    Booking postBooking(Booking booking, String bookerId);

    Booking patchBooking(String bookingId, String sharerId, boolean approved);

    Booking getBooking(String ownerOrBooker, String bookingId);

    Collection<Booking> getAllByBooker(String state, String booker);

    Collection<Booking> getAllForUser(String state, String user);

    Collection<Booking> getAllForBookerPageable(String from, String size, String booker);

    Collection<Booking> getAllForUserPageable(String from, String size, String user);
}
