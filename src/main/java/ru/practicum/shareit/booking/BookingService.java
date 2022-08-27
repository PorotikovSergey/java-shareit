package ru.practicum.shareit.booking;

public interface BookingService {
    Booking postBooking(Booking booking, String bookerId);

    Booking patchBooking(String bookingId, String sharerId, String approved);
}
