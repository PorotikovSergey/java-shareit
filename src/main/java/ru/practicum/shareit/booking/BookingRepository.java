package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBookingsByBookerId(long bookerId);
    List<Booking> findBookingsByItemOwnerId(long ownerId);
    List<Booking> findAllByBookerIdOrItemOwnerId(long bookerId, long ownerId);
    List<Booking> findAllByItemId(long itemId);
}
