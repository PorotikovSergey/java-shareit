package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestServiceImpl {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public Request postRequest(Request request, long requestorId) {
        checkDescription(request);
        request.setRequestor(userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет")));
        request.setCreated(new Date());
        requestRepository.save(request);
        return request;
    }

    public List<Request> getAll(long requestorId) {
        Set<Request> allRequests = new TreeSet<>((o1, o2) -> (o2.getCreated().compareTo(o1.getCreated())));
        List<Request> requestList = requestRepository.findRequestsByRequestorId(userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет")).getId());
        if (requestList.isEmpty()) {
            return new ArrayList<>();
        }
        for (Request request : requestList) {
            request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        }
        allRequests.addAll(requestList);
        return new ArrayList<>(allRequests);
    }

    public List<Request> getAllPageable(int from, int size, long requestorId) {
        Page<Request> page = requestRepository.findAll(PageRequest.of(from, size));
        List<Request> result = page.toList();
        for (Request request : result) {
            request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        }
        return result.stream()
                .filter(r -> r.getRequestor().getId() != userRepository.findById(requestorId)
                        .orElseThrow(() -> new NotFoundException("Такого юзера нет"))
                        .getId())
                .collect(Collectors.toList());
    }

    public Request getRequest(String itemRequestId, long requestorId) {
        long requestId = Long.parseLong(itemRequestId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Такого запроса нет"));

        checkUser(requestorId);

        request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        return request;
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким айди " + userId + " нет");
        }
    }

    private void checkDescription(Request request) {
        if ((request.getDescription() == null) || (request.getDescription().isBlank())) {
            throw new ValidationException("Описание должно быть!");
        }
    }
}

