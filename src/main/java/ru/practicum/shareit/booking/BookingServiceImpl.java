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
        System.out.println(itemRepository.findAll());
        System.out.println(booking);
        System.out.println("юзер "+bookerId+"- хочет забронировать вещь  "+itemRepository.findById(booking.getItemId()));
        if(!userRepository.existsById(Long.parseLong(bookerId))) {
            throw new NotFoundException("У айтема несуществующий букер");
        }
        if(!itemRepository.existsById(booking.getItemId())) {
            throw new NotFoundException("Нет айтема с таким id");
        }
        if(!itemRepository.existsById(booking.getItemId())) {
            throw new ValidationException("Такого айтема нет для бронирования");
        }
        if(!itemRepository.findById(booking.getItemId()).get().getAvailable()) {
            throw new ValidationException("Недоступный для бронирования");
        }
        if(!userRepository.existsById(itemRepository.findById(booking.getItemId()).get().getOwnerId())) {
            throw new NotFoundException("У айтема несуществующий владелец");
        }
        if((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        System.out.println("========\n==========\n=======\n==========");
        itemRepository.findAll();
        System.out.println(booking);
        return booking;
    }
}
