package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService{
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }
    @Override
    public Booking postBooking(Booking booking, String bookerId) {
        validateBooking(booking, bookerId);
        booking.setBookerId(Long.parseLong(bookerId));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        bookingRepository.findAll();
        return booking;
    }

    @Override
    public Booking patchBooking(String bookingId, String sharerId, String approved) {
        if(!bookingRepository.existsById(Long.parseLong(bookingId))) {
            throw new NotFoundException("Бронирования с таким id не существует");
        }
        if(Long.parseLong(sharerId)
                !=itemRepository.findById(bookingRepository.findById(Long.parseLong(bookingId)).get().getItemId()).get().getOwnerId()) {
            throw new ValidationException("Патчить статус вещи может только её владелец");
        }
        if(approved.equals("true")) {
            bookingRepository.findById(Long.parseLong(bookingId)).get().setStatus(BookingStatus.APPROVED);
        } else {
            bookingRepository.findById(Long.parseLong(bookingId)).get().setStatus(BookingStatus.REJECTED);
        }
        return bookingRepository.findById(Long.parseLong(bookingId)).get();
    }

    private void validateBooking(Booking booking, String bookerId) {
        if(!userRepository.existsById(Long.parseLong(bookerId))) {
            throw new NotFoundException("Букера с таким id не существует");
        }
        if(!itemRepository.existsById(booking.getItemId())) {
            throw new NotFoundException("Нет айтема с таким id");
        }
        if(!itemRepository.findById(booking.getItemId()).get().getAvailable()) {
            throw new ValidationException("Недоступный для бронирования");
        }
        if((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
    }
}
