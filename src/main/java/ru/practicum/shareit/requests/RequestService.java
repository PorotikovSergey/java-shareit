package ru.practicum.shareit.requests;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public interface RequestService {
    Request postRequest(Request request, String requestor);

    Collection<Request> getAll(String requestor);

    Collection<Request> getAllPageable(String from, String size);
}
