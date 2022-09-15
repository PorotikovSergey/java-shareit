package ru.practicum.shareit.booking;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    Booking bookingOne;
    Booking bookingTwo;
    Booking bookingThree;
    Booking bookingFour;

    @BeforeAll
    void beforeAll() throws InterruptedException {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        User userOne = new User(1L, "Bob", "bob@mail.ru");

        User userTwo = new User(2L, "Mary", "mary@ya.ru");
        User userThree = new User(3L, "John", "john@gmail.com");


        Item itemOne = new Item(1L, "item-1", "description-1", true, 1L, userOne,
                null, null, null);

        Item itemTwo = new Item(2L, "item-2", "description-2", true, 2L, userTwo,
                null, null, null);

        Item itemThree = new Item(3L, "item-3", "description-3", true, 3L, userThree,
                null, null, null);


        userRepository.save(userOne);
        itemRepository.save(itemOne);

        userRepository.save(userTwo);
        itemRepository.save(itemTwo);

        userRepository.save(userThree);
        itemRepository.save(itemThree);

        LocalDateTime start = LocalDateTime.now();
        Thread.sleep(10);
        LocalDateTime end = LocalDateTime.now();

        bookingOne = new Booking(1L, start, end, userOne, itemOne);
        bookingRepository.save(bookingOne);
        bookingTwo = new Booking(2L, start, end, userTwo, itemTwo);
        bookingRepository.save(bookingTwo);
        bookingThree = new Booking(3L, start, end, userThree, itemThree);
        bookingRepository.save(bookingThree);
        bookingFour = new Booking(4L, start, end, userOne, itemTwo);
        bookingRepository.save(bookingFour);
    }

    @Test
    void findAllByBookerIdOrItemOwnerId() { //1900
        List<Booking> list = bookingRepository.findAllByBookerIdOrItemOwnerId(1L, 3L);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(3, list.get(1).getId());
        System.out.println("вышли из 1900");
    }

    @Test
    void findAllByItemId() {  //88
        List<Booking> list = bookingRepository.findAllByItemId(3L);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(3, list.get(0).getId());
        System.out.println("вышли из 88");
    }

    @Test
    void findBookingsByItemOwnerId() {     //1
        List<Booking> list = bookingRepository.findBookingsByItemOwnerId(2L);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(2, list.get(0).getId());
        assertEquals(4, list.get(1).getId());
    }

    @Test
    void findBookingsByBookerId() {   //2
        List<Booking> list = bookingRepository.findBookingsByBookerId(1L);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals(4, list.get(1).getId());
    }
}