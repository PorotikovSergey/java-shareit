package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServiceException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemServiceImpl itemService;

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
    Booking futureBooking;

    Comment comment;

    List<Item> list = new ArrayList<>();
    List<Item> list2 = new ArrayList<>();
    List<Booking> bookings = new ArrayList<>();
    List<Booking> futureBookings = new ArrayList<>();

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        user1 = new User(1L, "Bob", "bobby@mail.ru");
        user2 = new User(2L, "John", "johny@ya.ru");
        user3 = new User(3L, "Mary", "marianna@hotmail.com");

        item1 = new Item(1L, "item-1", "description-1", true,
                13, user1, null, null, null);

        item2 = new Item(2L, "item-2", "description-2", false,
                37, user2, Collections.emptyList(), last, next);

        item3 = new Item(3L, "item-3", "description-3", true,
                12, null, Collections.emptyList(), booking1, next);

        LocalDateTime date = LocalDateTime.of(2021, 9, 17, 21, 30);

        booking1 = new Booking(100L, date, date, user1, null);
        booking2 = new Booking(200L, date, date, user2, null);

        futureBooking = new Booking(300L, date.plusDays(1000), date.plusDays(1000), user1, item1);

        list.add(item1);
        list.add(item2);
        list.add(item3);

        list2.add(item3);

        bookings.add(booking1);
        bookings.add(booking2);

        futureBookings.add(futureBooking);

        comment = new Comment(99L, item3, "Mikha", "text of comment");
    }

    @Test
    void getAll() {
        Mockito
                .when(itemRepository.findAll()).thenReturn(list);
        Mockito
                .when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(list);

        List<Item> testList = itemService.getAll("1", "0", "30");
        assertNotNull(testList);
        assertEquals(3, testList.size());
        assertTrue(testList.contains(item1));
        assertTrue(testList.contains(item2));
        assertTrue(testList.contains(item3));
    }

    @Test
    void postItem() {
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        Item testItem = itemService.postItem(item2, "2");
        assertNotNull(testItem);
        assertEquals(2L, testItem.getId());
        assertEquals("item-2", testItem.getName());
        assertEquals("description-2", testItem.getDescription());
        assertEquals(false, testItem.getAvailable());
        assertEquals(user2, testItem.getOwner());
        assertEquals(0, testItem.getComments().size());
        assertNull(testItem.getLastBooking());
        assertNull(testItem.getNextBooking());
    }

    @Test
    void deleteItem() {
        Mockito
                .doNothing().when(itemRepository).deleteById(anyLong());
        itemService.deleteItem(3L);
    }

    @Test
    void patchItem() {
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(itemRepository.save(item1)).thenReturn(item1);

        Item testItem = itemService.patchItem(1L, item1, "1");

        assertNotNull(testItem);
        assertEquals(1L, testItem.getId());
        assertEquals("item-1", testItem.getName());
        assertTrue(testItem.getAvailable());
    }

    @Test
    void patchItem2() {
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(itemRepository.save(item1)).thenReturn(item1);

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.patchItem(1L, item2, "3"));
        Assertions.assertEquals("Патчить вещь может только её владелец с айди 1", exception.getMessage());
    }

    @Test
    void patchItemNullOwner() {
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(itemRepository.save(item1)).thenReturn(item1);

        final ServiceException exception = Assertions.assertThrows(
                ServiceException.class,
                () -> itemService.patchItem(1L, item1, null));
        Assertions.assertEquals("Отсутствует владелец", exception.getMessage());
    }

    @Test
    void getItem() {
        Mockito
                .when(bookingRepository.findAllByBookerIdOrItemOwnerId(anyLong(), anyLong())).thenReturn(bookings);
        Mockito
                .when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        Mockito
                .when(commentRepository.findAllByItemId(anyLong())).thenReturn(new ArrayList<>());

        Item testItem = itemService.getItem("1", 2L);
        assertNotNull(testItem);
        assertEquals(1L, testItem.getId());
        assertEquals("item-1", testItem.getName());
        assertEquals("description-1", testItem.getDescription());
        assertEquals(true, testItem.getAvailable());
    }

    @Test
    void searchItem() {
        list.remove(item2);

        Mockito
                .when(itemRepository.findAllByNameContainingIgnoreCaseAndAvailableIs(anyString(), eq(true)))
                .thenReturn(list);
        Mockito.when(itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailableIs(anyString(), eq(true)))
                .thenReturn(list2);

        List<Item> searchedItems = itemService.searchItem("item-1", "1", null, "10");

        assertNotNull(searchedItems);
        assertEquals(2, searchedItems.size());
        assertTrue(searchedItems.contains(item1));
        assertTrue(searchedItems.contains(item3));
        assertFalse(searchedItems.contains(item2));
    }

    @Test
    void postCommentWrongTime() {
        Mockito
                .when(commentRepository.save(any())).thenReturn(comment);
        Mockito
                .when(bookingRepository.findAllByItemId(anyLong())).thenReturn(futureBookings);
        Mockito
                .when(itemRepository.findById(any())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postComment("1", 1, comment));
        Assertions.assertEquals("Отзывы возможны только к прошедшим броням", exception.getMessage());
    }

    @Test
    void postCommentNoItem() {
        Mockito
                .when(commentRepository.save(any())).thenReturn(comment);
        Mockito
                .when(bookingRepository.findAllByItemId(anyLong())).thenReturn(Collections.emptyList());
        Mockito
                .when(itemRepository.findById(any())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.postComment("1", 3, comment));
        Assertions.assertEquals("Бронирования на данный айтем не было", exception.getMessage());
    }

    @Test
    void postCommentNoText() {
        comment.setText("");
        Mockito
                .when(commentRepository.save(any())).thenReturn(comment);
        Mockito
                .when(bookingRepository.findAllByItemId(anyLong())).thenReturn(bookings);
        Mockito
                .when(itemRepository.findById(any())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postComment("1", 3, comment));
        Assertions.assertEquals("Текст отзыва не может быть пустым", exception.getMessage());
    }

    @Test
    void postComment() {
        Mockito
                .when(commentRepository.save(any())).thenReturn(comment);
        Mockito
                .when(bookingRepository.findAllByItemId(anyLong())).thenReturn(bookings);
        Mockito
                .when(itemRepository.findById(any())).thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        Comment testComment = itemService.postComment("1", 3, comment);

        assertNotNull(testComment);
        assertEquals(99L, testComment.getId());
        assertEquals("text of comment", testComment.getText());
        assertEquals(item1, testComment.getItem());
    }

    @Test
    void postItemToRequest() {
        Mockito
                .when(itemRepository.save(item3)).thenReturn(item3);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user3));

        Item testItem = itemService.postItemToRequest(item3, "3", 3);
        assertNotNull(testItem);
        assertEquals(3L, testItem.getId());
        assertEquals("item-3", testItem.getName());
        assertEquals("description-3", testItem.getDescription());
        assertEquals(true, testItem.getAvailable());
        assertEquals(3, testItem.getRequestId());
        assertEquals(user3, testItem.getOwner());
        assertEquals(0, testItem.getComments().size());
        assertNull(testItem.getLastBooking());
        assertNull(testItem.getNextBooking());
    }


    @Test
    void postCommentWithOutItem() {
        Mockito
                .when(userRepository.findById(100L)).thenThrow(new NotFoundException("Такого айтема нет"));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> userRepository.findById(100L));
        Assertions.assertEquals("Такого айтема нет", exception.getMessage());
    }

    @Test
    void postItemNullAvailable() {
        item2.setAvailable(null);
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postItem(item2, "2"));
        Assertions.assertEquals("Вещь без доступности.", exception.getMessage());
    }

    @Test
    void postItemNullName() {
        item2.setName(null);
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postItem(item2, "2"));
        Assertions.assertEquals("Вещь с пустым именем.", exception.getMessage());
    }

    @Test
    void postItemBlankName() {
        item2.setName("");
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postItem(item2, "2"));
        Assertions.assertEquals("Вещь с пустым именем.", exception.getMessage());
    }

    @Test
    void postItemNullDescription() {
        item2.setDescription(null);
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postItem(item2, "2"));
        Assertions.assertEquals("Вещь с пустым описанием", exception.getMessage());
    }

    @Test
    void postItemBlankDescription() {
        item2.setDescription("");
        Mockito
                .when(itemRepository.save(item2)).thenReturn(item2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.postItem(item2, "2"));
        Assertions.assertEquals("Вещь с пустым описанием", exception.getMessage());
    }
}