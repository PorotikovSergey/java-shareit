package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemServiceImpl itemService;

    @MockBean
    Mapper mapper;

    @Autowired
    private MockMvc mvc;

    User user;
    User user2;

    Item item;
    Item item2;

    ItemDto itemDto;

    Comment comment;
    CommentDto commentDto;

    Booking last;
    Booking next;

    List<Item> list = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user = new User(
                1L,
                "John",
                "john.doe@mail.com"
        );

        user2 = new User(
                2L,
                "John2",
                "john2@mail.com"
        );

        item = new Item(1L, "item-1", "description-1", true,
                13, user, null, null, null);

        item2 = new Item(2L, "item-2", "description-2", false,
                37, user2, Collections.emptyList(), last, next);

        itemDto = new ItemDto(11L, "itemDto", "descriptionDto", false,
                12, null, null, Collections.emptyList(), 99L);

        list.add(item);
        list.add(item2);

        comment = new Comment(12L, item2, "Bob", "this is comment");
        commentDto = new CommentDto(27L, "this is comment", "Bob");
    }

    @Test
    void postItem() throws Exception {
        when(mapper.fromItemToDto(any()))
                .thenReturn(itemDto);
        when(itemService.postItem(item, "1"))
                .thenReturn(item2);

        mvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(11)))
                .andExpect(jsonPath("$.name", is("itemDto")))
                .andExpect(jsonPath("$.description", is("descriptionDto")))
                .andExpect(jsonPath("$.available", is(false)))
                .andExpect(jsonPath("$.requestId", is(99)));

        verify(mapper, times(1)).fromItemToDto(any());
    }

    @Test
    void getAll() throws Exception {
        when(mapper.fromItemToDto(any()))
                .thenReturn(itemDto);
        when(mapper.fromBookingToDto(any()))
                .thenReturn(null);
        when(mapper.fromCommentToDto(any()))
                .thenReturn(null);
        when(itemService.getAll(anyString(), anyInt(), anyInt()))
                .thenReturn(list);

        mvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(11)))
                .andExpect(jsonPath("$[0].name", is("itemDto")))
                .andExpect(jsonPath("$[0].description", is("descriptionDto")))
                .andExpect(jsonPath("$[0].available", is(false)))
                .andExpect(jsonPath("$[1].id", is(11)))
                .andExpect(jsonPath("$[1].name", is("itemDto")))
                .andExpect(jsonPath("$[1].description", is("descriptionDto")))
                .andExpect(jsonPath("$[1].available", is(false)));
        ;

        verify(itemService, times(1)).getAll(any(), any(), any());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void deleteItem() throws Exception {
        doNothing().when(itemService).deleteItem(item.getId());

        mvc.perform(delete("/items/1"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void patchItem() throws Exception {
        when(mapper.fromItemToDto(any()))
                .thenReturn(itemDto);
        when(itemService.patchItem(anyLong(), any(Item.class), anyString()))
                .thenReturn(item2);

        mvc.perform(patch("/items/2")
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(99)));

        verify(itemService, times(1)).patchItem(2L, null, null);
        verify(mapper, times(1)).fromItemToDto(any());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getItem() throws Exception {
        when(mapper.fromItemToDto(item))
                .thenReturn(itemDto);
        when(itemService.getItem(any(), anyLong()))
                .thenReturn(item);

        mvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));

        verify(itemService, times(1)).getItem(any(), anyLong());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void searchItem() throws Exception {
        when(mapper.fromItemToDto(any()))
                .thenReturn(itemDto);
        when(mapper.fromBookingToDto(any()))
                .thenReturn(null);
        when(mapper.fromCommentToDto(any()))
                .thenReturn(null);
        when(itemService.searchItem(anyString(), any(), any(), any()))
                .thenReturn(list);

        mvc.perform(get("/items/search?text=qwerty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(11)))
                .andExpect(jsonPath("$[0].name", is("itemDto")))
                .andExpect(jsonPath("$[0].description", is("descriptionDto")))
                .andExpect(jsonPath("$[0].available", is(false)))
                .andExpect(jsonPath("$[1].id", is(11)))
                .andExpect(jsonPath("$[1].name", is("itemDto")))
                .andExpect(jsonPath("$[1].description", is("descriptionDto")))
                .andExpect(jsonPath("$[1].available", is(false)));
        ;

        verify(itemService, times(1)).searchItem(any(), any(), any(), any());
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void postComment() throws Exception {
        when(mapper.fromItemToDto(any()))
                .thenReturn(itemDto);
        when(mapper.fromCommentToDto(any()))
                .thenReturn(commentDto);
        when(itemService.postComment(anyString(), anyLong(), any()))
                .thenReturn(comment);

        mvc.perform(post("/items/2/comment")
                        .content(objectMapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(27)))
                .andExpect(jsonPath("$.authorName", is("Bob")))
                .andExpect(jsonPath("$.text", is("this is comment")));

        verify(itemService, times(1)).postComment(any(), anyLong(), any());
        verifyNoMoreInteractions(itemService);
    }
}