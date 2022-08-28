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
import java.util.*;

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
        if (!itemRepository.existsById(booking.getItemId())) {
            throw new NotFoundException("Айтема с таким id не существует");
        }
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
        Booking booking = bookingRepository.findById(Long.parseLong(bookingId)).get();
        User booker = userRepository.findById(booking.getBookerId()).get();
        Item item = itemRepository.findById(booking.getItemId()).get();
        if (approved.equals("true")) {
            booking.setStatus(BookingStatus.APPROVED);
        } else if (approved.equals("false")) {
            booking.setStatus(BookingStatus.REJECTED);
        }
        booking.setBooker(booker);
        booking.setItem(item);
        bookingRepository.save(booking);
        return bookingRepository.findById(Long.parseLong(bookingId)).get();
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        if (!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
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
        if (!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        for (Booking booking : bookingRepository.findAll()) {
            User booker = new User();
            booker.setId(booking.getBookerId());
            booker.setName(getBookerByBooking(booking).getName());
            booker.setEmail(getBookerByBooking(booking).getEmail());
            booking.setBooker(booker);
            Item item = new Item();
            item.setId(booking.getItemId());
            item.setName(getItemByBooking(booking).getName());
            item.setDescription(getItemByBooking(booking).getDescription());
            item.setAvailable(getItemByBooking(booking).getAvailable());
            item.setOwnerId(getItemByBooking(booking).getOwnerId());
            booking.setItem(item);
            result.add(booking);
        }
        return result;
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String booker) {
        try {
            BookingState bookingState = BookingState.valueOf(state.substring(6));
            switch (bookingState) {
                case FUTURE:
                    return getAll(booker);
                case CURRENT:
                    return getAll(booker);
                case WAITING:
                    return getAll(booker);
                case REJECTED:
                    return getAll(booker);
            }
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return getAll(booker);
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

    private Item getItemByBooking(Booking booking) {
        return itemRepository.getReferenceById(booking.getItemId());
    }

    private User getBookerByBooking(Booking booking) {
        return userRepository.getReferenceById(booking.getBookerId());
    }
}
