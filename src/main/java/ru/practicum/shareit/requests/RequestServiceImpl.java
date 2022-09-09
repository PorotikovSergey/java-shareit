package ru.practicum.shareit.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

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
        request.setId(requestorId);
        request.setCreateDate(LocalDateTime.now());
        requestRepository.save(request);
        return request;
    }
}
