package ru.practicum.shareit.requests;

import java.util.List;

public interface RequestService {
    Request postRequest(Request request, String requestor);

    List<Request> getAll(String requestor);

    List<Request> getAllPageable(int from, int size, String requestor);

    Request getRequest(String itemRequestId, String requestor);
}
