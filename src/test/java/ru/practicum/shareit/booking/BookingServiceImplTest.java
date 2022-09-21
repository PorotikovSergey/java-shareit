package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
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

        booking1 = new Booking(1L, start.plusDays(1000), end.plusDays(1000), user1, item1);
        booking2 = new Booking(2L, start.minusDays(1000), end.minusDays(1000), user2, item2);

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
    void postBookingWrongItem() {
        item1.setAvailable(false);
        Mockito
                .when(bookingRepository.save(any())).thenReturn(booking1);
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.postBooking(booking1, "3", 1));
        Assertions.assertEquals("Недоступный для бронирования. Айди вещи 1", exception.getMessage());
    }

    @Test
    void patchBooking() {
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        Booking testBooking = bookingService.patchBooking("1", "1", true);
        assertNotNull(testBooking);
        assertEquals(1L, testBooking.getId());
        assertEquals(2025, testBooking.getStart().getYear());
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
        assertEquals(2019, testBooking.getStart().getYear());
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


        List<Booking> testList = bookingService.getAllForBooker("All", 1, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerSetPast() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("PAST", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerSetFuture() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("FUTURE", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerSetCurrent() {
        List<Booking> currentList = new ArrayList<>();
        currentList.add(new Booking(3L, start.minusDays(1000), end.plusDays(1000), user2, item2));
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(currentList);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(currentList);


        List<Booking> testList = bookingService.getAllForBooker("CURRENT", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerNullStateFirst() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker(null, 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForNoBooker() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(false);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllForBooker("All", 1, 2, "1"));
        Assertions.assertEquals("Юзера с таким id 1 не существует", exception.getMessage());
    }

    @Test
    void getAllForBookerPast() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("PAST", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerFuture() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("FUTURE", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerCurrent() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("CURRENT", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerWaiting() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("WAITING", 0, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForBookerRejected() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        List<Booking> testList = bookingService.getAllForBooker("REJECTED", 0, 2, "1");
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

        List<Booking> testList = bookingService.getAllForOwner("All", 1, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForNoOwner() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(false);
        Mockito
                .when(bookingRepository.findBookingsByItemOwnerId(anyLong())).thenReturn(list);

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getAllForOwner("All", 1, 2, "1"));
        Assertions.assertEquals("Юзера с таким id 1 не существует", exception.getMessage());
    }

    @Test
    void getAllForOwnerWaiting() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByItemOwnerId(anyLong())).thenReturn(list);

        List<Booking> testList = bookingService.getAllForOwner("WAITING", 1, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void getAllForOwnerRejected() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByItemOwnerId(anyLong())).thenReturn(list);

        List<Booking> testList = bookingService.getAllForOwner("REJECTED", 1, 2, "1");
        assertNotNull(testList);
    }

    @Test
    void postBookingWithNoItem() {
        Mockito
                .when(bookingRepository.save(any())).thenReturn(booking1);
        Mockito
                .when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.postBooking(booking1, "1", 33));
        Assertions.assertEquals("Такого айтема нет", exception.getMessage());
    }

    @Test
    void postBookingWithNoUser() {
        Mockito
                .when(bookingRepository.save(any())).thenReturn(booking1);
        Mockito
                .when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.postBooking(booking1, "33", 1));
        Assertions.assertEquals("Такого юзера нет", exception.getMessage());
    }

    @Test
    void postBookingFromOwner() {
        Booking newBooking = new Booking();

        Mockito
                .when(bookingRepository.save(any())).thenReturn(newBooking);
        Mockito
                .when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.postBooking(newBooking, "1", 1));
        Assertions.assertEquals("Нельзя арендовать свою вещь у себя же самого", exception.getMessage());
    }

//    @Test
//    void postBookingFromPast() {
//        Booking pastBooking = new Booking();
//        pastBooking.setStart(LocalDateTime.of(1999, 9,9,9, 9));
//        pastBooking.setEnd(LocalDateTime.of(1998, 9,9,9, 9));
//
//        Mockito
//                .when(bookingRepository.save(any())).thenReturn(pastBooking);
//        Mockito
//                .when(itemRepository.findById(3L)).thenReturn(Optional.of(item3));
//        Mockito
//                .when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
//
//        final ValidationException exception = Assertions.assertThrows(
//                ValidationException.class,
//                () -> bookingService.postBooking(pastBooking, "1", 3L));
//        Assertions.assertEquals("Время аренды не может быть в прошлом", exception.getMessage());
//    }


    @Test
    void getAllForBookerWithWrongState() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getAllForBooker("unknownState", 0, 2, "1"));
        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

//    @Test
//    void getAllForBookerWithWrongSize() {
//        Mockito
//                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
//        Mockito
//                .when(userRepository.existsById(anyLong())).thenReturn(true);
//        Mockito
//                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);
//
//
//        final ValidationException exception = Assertions.assertThrows(
//                ValidationException.class,
//                () -> bookingService.getAllForBooker("REJECTED", 0, 1, "1"));
//        Assertions.assertEquals("Невалидные значения from и size", exception.getMessage());
//    }

    @Test
    void getAllForOwnerWithWrongState() {
        Mockito
                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito
                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);


        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.getAllForOwner("unknownState", 0, 2, "1"));
        Assertions.assertEquals("Unknown state: UNSUPPORTED_STATUS", exception.getMessage());
    }

//    @Test
//    void getAllForOwnerWithWrongSize() {
//        Mockito
//                .when(bookingRepository.findBookingsByBookerId(1)).thenReturn(list);
//        Mockito
//                .when(userRepository.existsById(anyLong())).thenReturn(true);
//        Mockito
//                .when(bookingRepository.findBookingsByBookerId(anyLong())).thenReturn(list);
//
//
//        final ValidationException exception = Assertions.assertThrows(
//                ValidationException.class,
//                () -> bookingService.getAllForOwner("REJECTED", 0, 1, "1"));
//        Assertions.assertEquals("Невалидные значения from и size", exception.getMessage());
//    }

    @Test
    void patchBookingNoAccess() {
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.patchBooking("1", "100", true));
        Assertions.assertEquals("Патчить статус вещи может владелец, а не пользователь с айди 100",
                exception.getMessage());
    }

    @Test
    void getBookingNoAcces() {
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking2));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> bookingService.getBooking("1", "2"));
        Assertions.assertEquals("Только владелец или арендатор могут просматривать айтем",
                exception.getMessage());
    }

    @Test
    void patchApprovedBooking() {
        booking2.setStatus(BookingStatus.APPROVED);
        Mockito
                .when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> bookingService.patchBooking("2", "2", true));
        Assertions.assertEquals("Нельзя менять статус уже подтверждённой брони",
                exception.getMessage());
    }
}
