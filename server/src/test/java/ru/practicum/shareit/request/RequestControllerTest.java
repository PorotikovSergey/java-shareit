package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RequestControllerTest {
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RequestServiceImpl requestService;
    @MockBean
    Mapper requestMapper;

    @Autowired
    private MockMvc mvc;

    User requestor;
    Request request;
    Request request2;
    RequestDto requestDto;
    RequestDto requestDto2;

    List<Request> list = new ArrayList<>();
    List<Request> pageableList = new ArrayList<>();
    Date date = Date.from(Instant.ofEpochSecond(1234567890));

    @BeforeEach
    void setUp() {
        requestor = new User(23L, "Bob", "Bob@mail.ru");
        request = new Request(1L, "description",
                date, requestor, Collections.emptyList());
        request2 = new Request(2L, "description-2",
                date, requestor, Collections.emptyList());
        requestDto = new RequestDto(22L, "description-22", 22,
                date, Collections.emptyList());
        requestDto2 = new RequestDto(23L, "description-23", 23,
                date, Collections.emptyList());

        list.add(request);
        list.add(request2);

        pageableList.add(request2);
    }


    @Test
    void postItemRequest() throws Exception {
        when(requestMapper.fromRequestToDto(any()))
                .thenReturn(requestDto);
        when(requestService.postRequest(request, 22L))
                .thenReturn(request);

        mvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(22)))
                .andExpect(jsonPath("$.description", is("description-22")))
                .andExpect(jsonPath("$.requestor", is(22)))
                .andExpect(jsonPath("$.created", is("2009-02-13T23:31:30.000+00:00")));

        verify(requestMapper, times(1)).fromRequestToDto(any());
        verify(requestService, times(1)).postRequest(any(), anyLong());
        verifyNoMoreInteractions(requestService);
    }

    @Test
    void getAll() throws Exception {
        when(requestMapper.fromRequestToDto(request))
                .thenReturn(requestDto);
        when(requestMapper.fromRequestToDto(request2))
                .thenReturn(requestDto2);
        when(requestService.getAll(anyLong()))
                .thenReturn(list);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(22)))
                .andExpect(jsonPath("$[0].description", is("description-22")))
                .andExpect(jsonPath("$[0].created", is("2009-02-13T23:31:30.000+00:00")))
                .andExpect(jsonPath("$[1].id", is(23)))
                .andExpect(jsonPath("$[1].description", is("description-23")))
                .andExpect(jsonPath("$[1].created", is("2009-02-13T23:31:30.000+00:00")));


        verify(requestService, times(1)).getAll(anyLong());
        verifyNoMoreInteractions(requestService);
    }

    @Test
    void getItemRequest() throws Exception {
        when(requestMapper.fromRequestToDto(request2))
                .thenReturn(requestDto);
        when(requestService.getRequest(anyString(), anyLong()))
                .thenReturn(request2);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(22)))
                .andExpect(jsonPath("$.description", is("description-22")))
                .andExpect(jsonPath("$.created", is("2009-02-13T23:31:30.000+00:00")))
                .andExpect(jsonPath("$.items", is(Collections.emptyList())));

        verify(requestService, times(1)).getRequest(anyString(), anyLong());
        verifyNoMoreInteractions(requestService);
    }

    @Test
    void getAllPageable() throws Exception {
        when(requestMapper.fromRequestToDto(any()))
                .thenReturn(requestDto);
        when(requestService.getAllPageable(anyInt(), anyInt(), anyLong()))
                .thenReturn(pageableList);

        this.mvc.perform(get("/requests/all?from=0&size=2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(22)))
                .andExpect(jsonPath("$[0].description", is("description-22")))
                .andExpect(jsonPath("$[0].created", is("2009-02-13T23:31:30.000+00:00")));

        verify(requestService, times(1)).getAllPageable(anyInt(), anyInt(), anyLong());
        verifyNoMoreInteractions(requestService);
    }

}
