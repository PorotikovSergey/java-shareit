package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.requests.Request;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    BookingServiceImpl bookingService;

    User user1;
    User user2;
    User user3;

    Item item1;
    Item item2;
    Item item3;

    Booking last;
    Booking next;

    Booking booking1;
    Booking booking2;

    Request request1;
    Request request2;

    LocalDateTime start;
    LocalDateTime end;


    List<Booking> list = new ArrayList<>();
    List<Item> itemList = new ArrayList<>();
    List<User> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, itemRepository, userRepository);

        user1 = new User(1L, "Bob", "bobby@mail.ru");
        user2 = new User(2L, "John", "johny@ya.ru");
        user3 = new User(3L, "Mary", "marianna@hotmail.com");

        users.add(user1);
        users.add(user2);
        users.add(user3);

        start = LocalDateTime.now();
        end = start.plusDays(1);


        item1 = new Item(1L, "item-1", "description-1", true,
                13, user1, null, null, null);

        item2 = new Item(2L, "item-2", "description-2", false,
                37, user2, Collections.emptyList(), last, next);

        item3 = new Item(3L, "item-3", "description-3", true,
                12, null, Collections.emptyList(), booking1, next);

        booking1 = new Booking(1L, start.plusDays(100), end.plusDays(101), user1, item1);
        booking2 = new Booking(2L, start, end, user2, item2);

        itemList.add(item1);
        itemList.add(item2);

        Date date = Date.from(Instant.now());

        request1 = new Request(1L, "description-1", date, user1, new ArrayList<>());
        request2 = new Request(2L, "description-2", date, user1, itemList);

        list.add(booking1);
        list.add(booking2);
    }

    @Test
    void postBooking() {
        Mockito
                .when(bookingRepository.save(any())).thenReturn(booking1);
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        Booking testBooking = bookingService.postBooking(booking1, "3", 1);
        assertNotNull(testBooking);
        assertEquals(1L, testBooking.getId());
        assertEquals(user1, testBooking.getBooker());
        assertEquals(item1, testBooking.getItem());
    }

    @Test
    void patchBooking() {
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        Booking testBooking = bookingService.patchBooking("1", "1", true);
        assertNotNull(testBooking);
        assertEquals(1L, testBooking.getId());
        assertEquals(2022, testBooking.getStart().getYear());
        assertEquals(user1, testBooking.getBooker());
        assertEquals(item1, testBooking.getItem());


    }

    @Test
    void getBooking() {
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking2));

        Booking testBooking = bookingService.getBooking("2", "2");
        assertNotNull(testBooking);
        assertEquals(2L, testBooking.getId());
        assertEquals(2022, testBooking.getStart().getYear());
        assertEquals(user2, testBooking.getBooker());
        assertEquals(item2, testBooking.getItem());
    }

    @Test
    void getAllForBooker() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("All", "1", "2", "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForOwner() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByItemOwnerId(anyLong())).thenReturn(list);

        List<Booking> testList = bookingService.getAllForOwner("All", "1", "2", "1");
        assertNotNull(testList);
    }
}