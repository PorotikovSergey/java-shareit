package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingMapper bookingMapper;
    @MockBean
    BookingServiceImpl bookingService;

    @Autowired
    private MockMvc mvc;

    LocalDateTime start = LocalDateTime.of(2023, 9, 17, 21, 30);

    User user1;
    User user2;

    Item item1;
    Item item2;

    Booking booking1;
    Booking booking2;

    BookingDto bookingDto;
    BookingDto bookingDto2;

    LocalDateTime end = LocalDateTime.of(2022, 9, 17, 21, 30);

    List<Booking> list = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user1 = new User(
                1L,
                "John",
                "john.doe@mail.com"
        );

        user2 = new User(
                2L,
                "John2",
                "john2@mail.com"
        );

        item1 = new Item(1L, "item-1", "description-1", true,
                13, user1, null, null, null);

        item2 = new Item(2L, "item-2", "description-2", false,
                37, user2, Collections.emptyList(), null, null);

        booking1 = new Booking(100L, start, end, user1, item1);
        booking2 = new Booking(200L, start, end, user2, item2);

        bookingDto = new BookingDto(300L, start, end, 2L, 2L,
                user2, item2, BookingStatus.WAITING, BookingState.ALL);

        bookingDto2 = new BookingDto(400L, start, end, 1L, 1L,
                user1, item1, BookingStatus.REJECTED, BookingState.FUTURE);

        list.add(booking1);
        list.add(booking2);
    }

    @Test
    void postBooking() throws Exception {
        when(bookingMapper.fromBookingToDto(any()))
                .thenReturn(bookingDto);
        when(bookingService.postBooking(any(), anyString(), anyLong()))
                .thenReturn(booking1);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(300)))
                .andExpect(jsonPath("$.start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$.end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$.bookerId", is(2)))
                .andExpect(jsonPath("$.itemId", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.state", is("ALL")));

        verify(bookingMapper, times(1)).fromBookingToDto(any());
    }

    @Test
    void patchBooking() throws Exception {
        when(bookingMapper.fromBookingToDto(any()))
                .thenReturn(bookingDto2);
        when(bookingService.patchBooking(anyString(), anyString(), anyBoolean()))
                .thenReturn(booking2);

        mvc.perform(patch("/bookings/bookingId?approved=true")
                        .content(mapper.writeValueAsString(booking1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(400)))
                .andExpect(jsonPath("$.start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$.end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$.itemId", is(1)))
                .andExpect(jsonPath("$.bookerId", is(1)))
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.state", is("FUTURE")));

        verify(bookingService, times(1)).patchBooking(anyString(), eq(null), eq(true));
        verify(bookingMapper, times(1)).fromBookingToDto(any());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getBooking() throws Exception {
        when(bookingMapper.fromBookingToDto(booking2))
                .thenReturn(bookingDto);
        when(bookingService.getBooking(any(), anyString()))
                .thenReturn(booking2);

        mvc.perform(get("/bookings/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(300)))
                .andExpect(jsonPath("$.itemId", is(2)))
                .andExpect(jsonPath("$.start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$.end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$.bookerId", is(2)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.state", is("ALL")));

        verify(bookingService, times(1)).getBooking(eq(null), anyString());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getAll() throws Exception {
        when(bookingMapper.fromBookingToDto(any()))
                .thenReturn(bookingDto);
        when(bookingService.getAllForBooker(any(), any(), any(), any()))
                .thenReturn(list);

        mvc.perform(get("/bookings?from=10&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(300)))
                .andExpect(jsonPath("$[0].itemId", is(2)))
                .andExpect(jsonPath("$[0].start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$[0].bookerId", is(2)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[0].state", is("ALL")));

        verify(bookingService, times(1)).getAllForBooker(any(), any(), any(), any());
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getForCurrentUser() throws Exception {
        when(bookingMapper.fromBookingToDto(booking2))
                .thenReturn(bookingDto2);
        when(bookingMapper.fromBookingToDto(booking1))
                .thenReturn(bookingDto);
        when(bookingService.getAllForOwner(any(), anyString(), anyString(), any()))
                .thenReturn(list);

        mvc.perform(get("/bookings/owner?from=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(300)))
                .andExpect(jsonPath("$[0].itemId", is(2)))
                .andExpect(jsonPath("$[0].start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$[0].end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$[0].bookerId", is(2)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[0].state", is("ALL")))
                .andExpect(jsonPath("$[1].id", is(400)))
                .andExpect(jsonPath("$[1].itemId", is(1)))
                .andExpect(jsonPath("$[1].start", is("2023-09-17T21:30:00")))
                .andExpect(jsonPath("$[1].end", is("2022-09-17T21:30:00")))
                .andExpect(jsonPath("$[1].bookerId", is(1)))
                .andExpect(jsonPath("$[1].status", is("REJECTED")))
                .andExpect(jsonPath("$[1].state", is("FUTURE")));

        verify(bookingService, times(1)).getAllForOwner(any(), anyString(), anyString(), any());
        verifyNoMoreInteractions(bookingService);
    }
}
