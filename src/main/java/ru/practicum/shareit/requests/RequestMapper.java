package ru.practicum.shareit.requests;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class RequestMapper {
    public RequestDto fromRequestToDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
//        requestDto.setRequestor(request.getRequestor());
        requestDto.setCreated(request.getCreated());
        requestDto.setDescription(request.getDescription());
//        requestDto.setItems(request.getItems());
        return requestDto;
    }

    public Request fromDtoToRequest(RequestDto requestDto) {
        Request request = new Request();
        request.setId(requestDto.getId());
//        request.setRequestor(requestDto.getRequestor());
        request.setCreated(requestDto.getCreated());
        request.setDescription(requestDto.getDescription());
        return request;
    }
}
