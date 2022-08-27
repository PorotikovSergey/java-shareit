package ru.practicum.shareit.booking;

import java.util.Collection;

public interface BookingService {
    Booking postBooking(Booking booking, String bookerId);

    Booking patchBooking(String bookingId, String sharerId, String approved);

    Booking getBooking(String ownerOrBooker, String bookingId);

    Collection<Booking> getAll(String ownerOrBooker);
}
