package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    RequestRepository requestRepository;

    RequestServiceImpl requestService;

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


    List<Request> list = new ArrayList<>();
    List<Item> itemList = new ArrayList<>();
    List<User> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        requestService = new RequestServiceImpl(requestRepository, userRepository, itemRepository);

        user1 = new User(1L, "Bob", "bobby@mail.ru");
        user2 = new User(2L, "John", "johny@ya.ru");
        user3 = new User(3L, "Mary", "marianna@hotmail.com");

        users.add(user1);
        users.add(user2);
        users.add(user3);

        item1 = new Item(1L, "item-1", "description-1", true,
                13, user1, null, null, null);

        item2 = new Item(2L, "item-2", "description-2", false,
                37, user2, Collections.emptyList(), last, next);

        item3 = new Item(3L, "item-3", "description-3", true,
                12, null, Collections.emptyList(), booking1, next);

        itemList.add(item1);
        itemList.add(item2);

        Date date = Date.from(Instant.now());

        request1 = new Request(1L, "description-1", date, user1, new ArrayList<>());
        request2 = new Request(2L, "description-2", date, user1, itemList);


        list.add(request1);
        list.add(request2);
    }

    @Test
    void postRequest() {
        Mockito
                .when(requestRepository.save(any())).thenReturn(request2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        Request testRequest = requestService.postRequest(request2, "2");
        assertNotNull(testRequest);
        assertEquals(2L, testRequest.getId());
        assertEquals("description-2", testRequest.getDescription());
        assertEquals(request2.getCreated(), testRequest.getCreated());
        assertEquals(2, testRequest.getItems().size());
    }

    @Test
    void getAll() {
        Mockito
                .when(requestRepository.findAll()).thenReturn(list);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user1));
        Mockito
                .when(requestRepository.findRequestsByRequestorId(anyLong())).thenReturn(list);
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);

        List<Request> testList = requestService.getAll("1");
        assertNotNull(testList);
        assertEquals(1, testList.size());
        assertTrue(testList.contains(request1));
    }

    @Test
    void getAllEmpty() {
        Mockito
                .when(requestRepository.findAll()).thenReturn(list);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user1));
        Mockito
                .when(requestRepository.findRequestsByRequestorId(anyLong())).thenReturn(Collections.emptyList());
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);

        List<Request> testList = requestService.getAll("1");
        assertNotNull(testList);
        assertEquals(0, testList.size());
    }

    @Test
    void getAllPageable() {
        Pageable pageable = PageRequest.of(1, 10);
        Mockito
                .when(requestRepository.findAll(pageable)).thenReturn(Page.empty());
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);
        Mockito
                .when(userRepository.findById(any())).thenReturn(Optional.of(user1));

        List<Request> resultList = requestService.getAllPageable("1", "10", "1");
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    void getAllPageableNullFromSize() {
        Pageable pageable = PageRequest.of(1, 10);
        Mockito
                .when(requestRepository.findAll(pageable)).thenReturn(Page.empty());
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);
        Mockito
                .when(userRepository.findById(any())).thenReturn(Optional.of(user1));

        List<Request> resultList = requestService.getAllPageable(null, null, "1");
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    void getRequest() {
        Mockito
                .when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request2));
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(true);

        Request testRequest = requestService.getRequest("1", "1");
        assertNotNull(testRequest);
        assertEquals(2L, testRequest.getId());
        assertEquals("description-2", testRequest.getDescription());
        assertEquals(request2.getCreated(), testRequest.getCreated());
        assertEquals(user1, testRequest.getRequestor());
    }

    @Test
    void getRequestWrongUser() {

        Mockito
                .when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request2));
        Mockito
                .when(itemRepository.findAllByRequestId(anyLong())).thenReturn(itemList);
        Mockito
                .when(userRepository.existsById(anyLong())).thenReturn(false);

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> requestService.getRequest("1", "1"));
        Assertions.assertEquals("Юзера с таким айди 1 нет", exception.getMessage());
    }

    @Test
    void postRequestWrongDescription() {
        request2.setDescription(null);
        Mockito
                .when(requestRepository.save(any())).thenReturn(request2);
        Mockito
                .when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> requestService.postRequest(request2, "2"));
        Assertions.assertEquals("Описание должно быть!", exception.getMessage());
    }
}