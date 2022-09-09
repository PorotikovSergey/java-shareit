package ru.practicum.shareit.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestServiceImpl(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
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
        System.out.println("\n\n"+request+"\n\n");
        return request;
    }

    @Override
    public Collection<Request> getAll(String requestor) {
        long requestorId = Long.parseLong(requestor);
        if (!userRepository.existsById(requestorId)) {
            throw new NotFoundException("Юзера с таким айди "+requestorId+" нет");
        }
        Set<Request> allRequsets = new TreeSet<>((o1, o2) -> (o2.getCreated().compareTo(o1.getCreated())));
        allRequsets.addAll(requestRepository.findRequestsByRequestor(requestorId));
        return allRequsets;
    }

    @Override
    public Collection<Request> getAllPageable(String from, String size) {
        if ((from==null)||(size==null)) {
            return new ArrayList<>();
        }
        int fromPage = Integer.parseInt(from);
        int sizePage = Integer.parseInt(size);
        Page<Request> page = requestRepository.findAll(PageRequest.of(fromPage, sizePage));
        return page.toList();
    }
}
