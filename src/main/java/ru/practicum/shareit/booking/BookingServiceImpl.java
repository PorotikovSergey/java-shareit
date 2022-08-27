package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
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
        Booking result = bookingRepository.findById(booking.getId()).get();
        User booker = userRepository.findById(Long.parseLong(bookerId)).get();
        Item item = itemRepository.findById(result.getItemId()).get();
        result.setBooker(booker);
        result.setItem(item);
        return result;
    }

    @Override
    public Booking patchBooking(String bookingId, String sharerId, String approved) {
        if (!bookingRepository.existsById(Long.parseLong(bookingId))) {
            throw new NotFoundException("Бронирования с таким id не существует");
        }
        if (Long.parseLong(sharerId)
                != itemRepository.findById(bookingRepository.findById(Long.parseLong(bookingId)).get().getItemId()).get().getOwnerId()) {
            throw new ValidationException("Патчить статус вещи может только её владелец");
        }
        User booker = userRepository.findById(bookingRepository.findById(Long.parseLong(bookingId)).get().getBookerId()).get();
        Item item = itemRepository.findById(bookingRepository.findById(Long.parseLong(bookingId)).get().getItemId()).get();
        if (approved.equals("true")) {
            bookingRepository.findById(Long.parseLong(bookingId)).get().setStatus(BookingStatus.APPROVED);
        } else {
            bookingRepository.findById(Long.parseLong(bookingId)).get().setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.findById(Long.parseLong(bookingId)).get().setBooker(booker);
        bookingRepository.findById(Long.parseLong(bookingId)).get().setItem(item);
        return bookingRepository.findById(Long.parseLong(bookingId)).get();
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        if(!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        if (!bookingRepository.existsById(Long.parseLong(bookingId))) {
            throw new NotFoundException("Букинга с таким id не существует");
        }
        Booking booking = bookingRepository.findById(Long.parseLong(bookingId)).get();
        if ((booking.getBookerId() == Long.parseLong(ownerOrBooker))
                || (itemRepository.findById(booking.getItemId()).get().getOwnerId() == Long.parseLong(ownerOrBooker))) {
            User booker = userRepository.findById(booking.getBookerId()).get();
            Item item = itemRepository.findById(booking.getItemId()).get();
            booking.setBooker(booker);
            booking.setItem(item);
            return bookingRepository.findById(Long.parseLong(bookingId)).get();
        } else {
            throw new ValidationException("Только владелец или арендатор могут просматривать айтем");
        }
    }

    @Override
    public Collection<Booking> getAll(String ownerOrBooker) {
        if(!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
//        Collection<Long> items = itemRepository.findAll().stream().filter(i -> i.getOwnerId() == Long.parseLong(ownerOrBooker)).map(Item::getOwnerId).collect(Collectors.toList());
//        Collection<Booking> collectionOne = bookingRepository.findAll().stream().filter(b -> b.getBookerId()==Long.parseLong(ownerOrBooker)).collect(Collectors.toList());
//        Collection<Booking> collectionTwo = bookingRepository.findAll().stream().filter(b -> items.contains(ownerOrBooker)).collect(Collectors.toList());
//        collectionOne.addAll(collectionTwo);
//        return collectionOne;
        return bookingRepository.findAll();
     }

    private void validateBooking(Booking booking, String bookerId) {
        if (!userRepository.existsById(Long.parseLong(bookerId))) {
            throw new NotFoundException("Букера с таким id не существует");
        }
        if (!itemRepository.existsById(booking.getItemId())) {
            throw new NotFoundException("Нет айтема с таким id");
        }
        if (!itemRepository.findById(booking.getItemId()).get().getAvailable()) {
            throw new ValidationException("Недоступный для бронирования");
        }
        if ((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
    }
}
