package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    Booking bookingOne;
    Booking bookingTwo;
    Booking bookingThree;
    Booking bookingFour;

    @BeforeEach
    void beforeEach() {
        bookingOne = new Booking(1L, 1L, 1L, 3L);
        bookingRepository.save(bookingOne);
        bookingTwo = new Booking(2L, 2L, 1L, 3L);
        bookingRepository.save(bookingTwo);
        bookingThree = new Booking(3L, 3L, 3L, 1L);
        bookingRepository.save(bookingThree);
        bookingFour = new Booking(4L, 4L, 3L, 1L);
        bookingRepository.save(bookingFour);
    }

    @Test
    void findAllByBookerIdOrItemOwnerId() {
        List<Booking> list = bookingRepository.findAllByBookerIdOrItemOwnerId(1L, 3L);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(3, list.get(0).getId());
        assertEquals(4, list.get(1).getId());
    }

    @Test
    void findAllByItemId() {
        List<Booking> list = bookingRepository.findAllByItemId(4L);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(8, list.get(0).getId());
    }

    @Test
    void findBookingsByItemOwnerId() {     //1
        List<Booking> list = bookingRepository.findBookingsByItemOwnerId(1L);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(9, list.get(0).getId());
        assertEquals(10, list.get(1).getId());
    }

    @Test
    void findBookingsByBookerId() {   //2
        List<Booking> list = bookingRepository.findBookingsByBookerId(1);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(15, list.get(0).getId());
        assertEquals(16, list.get(1).getId());
    }

    @AfterEach
    void afterEach() {
        bookingRepository.deleteAll();
        //почему-то не работает очистка репозитория
    }
}