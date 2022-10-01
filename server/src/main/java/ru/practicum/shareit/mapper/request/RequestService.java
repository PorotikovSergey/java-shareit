package ru.practicum.shareit.mapper.request;

import java.util.List;

public interface RequestService {
    Request postRequest(Request request, String requestor);

    List<Request> getAll(String requestor);

    List<Request> getAllPageable(String from, String size, String requestor);

    Request getRequest(String itemRequestId, String requestor);
}

