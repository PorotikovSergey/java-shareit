//package ru.practicum.shareit.requests;
//
//import org.junit.jupiter.api.Test;
//import ru.practicum.shareit.mapper.Mapper;
//import ru.practicum.shareit.user.User;
//
//import java.time.Instant;
//import java.util.Date;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class RequestMapperTest {
//    Mapper mapper = new Mapper();
//
//    User requestor1 = new User(1L, "bob", "bob@mail.ru");
//
//    Request request = new Request(1L, "request-1", Date.from(Instant.now()), requestor1, null);
//    RequestDto requestDto = new RequestDto(2L, "request-2", 2L, Date.from(Instant.now()), null);
//    Date testDate = request.getCreated();
//    Date testDtoDate = requestDto.getCreated();
//
//    @Test
//    void fromRequestToDto() {
//        RequestDto newDto = mapper.fromRequestToDto(request);
//
//        assertNotNull(newDto);
//        assertEquals(1L, newDto.getId());
//        assertEquals("request-1", newDto.getDescription());
//        assertEquals(testDate, newDto.getCreated());
//        assertNull(newDto.getItems());
//    }
//
//    @Test
//    void fromDtoToUser() {
//        Request newRequest = mapper.fromDtoToRequest(requestDto);
//
//        assertNotNull(newRequest);
//        assertEquals(2L, newRequest.getId());
//        assertEquals("request-2", newRequest.getDescription());
//        assertEquals(testDtoDate, newRequest.getCreated());
//    }
//}