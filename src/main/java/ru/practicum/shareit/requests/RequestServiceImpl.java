package ru.practicum.shareit.requests;

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
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Request postRequest(Request request, String requestor) {
        long requestorId = Long.parseLong(requestor);
        if (!userRepository.existsById(requestorId)) {
           throw new NotFoundException("Юзера с таким айди "+requestorId+" нет");
        }
        if ((request.getDescription()==null) || (request.getDescription().isBlank())) {
            throw new ValidationException("Описание должно быть!");
        }
        request.setRequestor(requestorId);
        request.setCreated(new Date());
        requestRepository.save(request);
        return request;
    }

    @Override
    public Collection<Request> getAll(String requestor) {
        long requestorId = Long.parseLong(requestor);
        if (!userRepository.existsById(requestorId)) {
            throw new NotFoundException("Юзера с таким айди "+requestorId+" нет");
        }
        Set<Request> allRequests = new TreeSet<>((o1, o2) -> (o2.getCreated().compareTo(o1.getCreated())));
        List<Request> requestList = requestRepository.findRequestsByRequestor(requestorId);
        for(Request request: requestList) {
            request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        }
        allRequests.addAll(requestList);
        return allRequests;
    }

    @Override
    public Collection<Request> getAllPageable(String from, String size, String requestor) {
        long requestorId = Long.parseLong(requestor);
        if (!userRepository.existsById(requestorId)) {
            throw new NotFoundException("Юзера с таким айди "+requestorId+" нет");
        }
        if ((from==null)||(size==null)) {
            return new ArrayList<>();
        }
        int fromPage = Integer.parseInt(from);
        int sizePage = Integer.parseInt(size);
        Page<Request> page = requestRepository.findAll(PageRequest.of(fromPage, sizePage));
        List<Request> result = page.toList();
        for(Request request: result) {
            request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        }
        return result.stream().filter(r -> r.getRequestor()!=requestorId).collect(Collectors.toList());
    }

    @Override
    public Request getRequest(String itemRequestId, String requestor) {

        long itemRequest = Long.parseLong(itemRequestId);
        if(!requestRepository.existsById(itemRequest)) {
            throw new NotFoundException("Реквеста с айди "+itemRequest+" нет");
        }
        long requestorId = Long.parseLong(requestor);
        if (!userRepository.existsById(requestorId)) {
            throw new NotFoundException("Юзера с таким айди "+requestorId+" нет");
        }
        Request request = requestRepository.findAll().stream().filter(r -> r.getId()==itemRequest).findFirst().get();
        request.getItems().addAll(itemRepository.findAllByRequestId(request.getId()));
        return request;
    }
}
