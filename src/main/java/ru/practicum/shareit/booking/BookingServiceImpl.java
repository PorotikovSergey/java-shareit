package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository, ItemMapper itemMapper, UserMapper userMapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Booking postBooking(Booking booking, String bookerId) {
        checkItem(booking.getItemId());
        long ownerId = itemRepository.getReferenceById(booking.getItemId()).getOwnerId();
        long itemBookerId = Long.parseLong(bookerId);

        if (ownerId == itemBookerId) {
            throw new NotFoundException("Нельзя бронировать свою же вещь. " +
                    "Айди владельца " + ownerId + ". Айди  желающего " + itemBookerId);
        }

        validateBooking(booking, bookerId);
        booking.setBookerId(itemBookerId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItemOwnerId(ownerId);
        return bookingMaker(booking);
    }

    @Override
    public Booking patchBooking(String parsedBookingId, String parsedOwnerId, boolean approved) {
        long idOfBooking = Long.parseLong(parsedBookingId);
        long idOfOwner = Long.parseLong(parsedOwnerId);

        checkBooking(idOfBooking);

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if (idOfOwner != booking.getItemOwnerId()) {
            throw new NotFoundException("Патчить статус вещи может владелец, а не пользователь с айди " + idOfOwner);
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMaker(booking);
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        long idOfBooking = Long.parseLong(bookingId);
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);

        checkUser(idOfOwnerOrBooker);

        checkBooking(idOfBooking);

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if (!((booking.getBookerId() == idOfOwnerOrBooker) || (booking.getItemOwnerId() == idOfOwnerOrBooker))) {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем. " +
                    "Айди владельца " + booking.getItemOwnerId() + ". " +
                    "Айди арендатора " + booking.getBookerId() + ". " +
                    "Айди желающего " + idOfOwnerOrBooker);
        }

        return bookingMaker(booking);
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String booker) {
        BookingState bookingState;
        Collection<Booking> allBookings = getAllBookingsForBooker(booker);

        for (Booking booking : allBookings) {
            bookingMaker(booking);
        }
        if (state == null) {
            return allBookings;
        }

        try {
            bookingState = BookingState.valueOf(state.substring(6));
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }

        switch (bookingState) {
            case PAST:
                Collection<Booking> pastBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getEnd().isBefore(now)) {
                        booking.setState(BookingState.PAST);
                        pastBookings.add(booking);
                    }
                }
                return pastBookings;
            case FUTURE:
                Collection<Booking> futureBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isAfter(now)) {
                        booking.setState(BookingState.FUTURE);
                        futureBookings.add(booking);
                    }
                }
                return futureBookings;
            case CURRENT:
                Collection<Booking> currentBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                        booking.setState(BookingState.CURRENT);
                        currentBookings.add(booking);
                    }
                }
                return currentBookings;
            case WAITING:
                Collection<Booking> allWaitingBookings = allBookings.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                        .collect(Collectors.toList());
                List<Booking> resultWaitingList = new ArrayList<>();
                for (Booking booking : allWaitingBookings) {
                    resultWaitingList.add(bookingMaker(booking));
                }
                return resultWaitingList;
            case REJECTED:
                Collection<Booking> allRejectedBookings = allBookings.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                        .collect(Collectors.toList());
                List<Booking> resultRejectedBookings = new ArrayList<>();
                for (Booking booking : allRejectedBookings) {
                    resultRejectedBookings.add(bookingMaker(booking));
                }
                return resultRejectedBookings;
        }
        return allBookings;
    }

    @Override
    public Collection<Booking> getAllForUser(String state, String user) {
        Collection<Booking> allBookings = getAllBookingsByOwnerId(user);

        for (Booking booking : allBookings) {
            bookingMaker(booking);
        }

        if (state == null) {
            return allBookings;
        }

        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.substring(6));
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }

        switch (bookingState) {
            case PAST:
                Collection<Booking> pastBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getEnd().isBefore(now)) {
                        booking.setState(BookingState.PAST);
                        pastBookings.add(booking);
                    }
                }
                return pastBookings;
            case FUTURE:
                Collection<Booking> futureBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isAfter(now)) {
                        booking.setState(BookingState.FUTURE);
                        futureBookings.add(booking);
                    }
                }
                return futureBookings;
            case CURRENT:
                Collection<Booking> currentBookings = new ArrayList<>();
                for (Booking booking : allBookings) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                        booking.setState(BookingState.CURRENT);
                        currentBookings.add(booking);
                    }
                }
                return currentBookings;
            case WAITING:
                Collection<Booking> waitingBookings = allBookings.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                        .collect(Collectors.toList());
                Collection<Booking> resultWaitingBookings = new ArrayList<>();
                for (Booking booking : waitingBookings) {
                    resultWaitingBookings.add(bookingMaker(booking));
                }
                return resultWaitingBookings;
            case REJECTED:
                Collection<Booking> rejectedBookings = allBookings.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                        .collect(Collectors.toList());
                Collection<Booking> resultRejectedBookings = new ArrayList<>();
                for (Booking booking : rejectedBookings) {
                    resultRejectedBookings.add(bookingMaker(booking));
                }
                return resultRejectedBookings;
        }
        return allBookings;
    }

    private void validateBooking(Booking booking, String bookerId) {
        checkUser(Long.parseLong(bookerId));
        checkItem(booking.getItemId());
        if (!itemRepository.getReferenceById(booking.getItemId()).getAvailable()) {
            throw new ValidationException("Недоступный для бронирования. Айди вещи " + booking.getItemId());
        }
        if ((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким айди " + userId + " нет в нашей базе");
        }
    }

    private void checkItem(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Айтема с таким айди " + itemId + " нет в нашей базе");
        }
    }

    private void checkBooking(long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Букинга с таким айди " + bookingId + " нет в нашей базе");
        }
    }

    private Collection<Booking> getAllBookingsForBooker(String booker) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfBooker = Long.parseLong(booker);
        if (!userRepository.existsById(idOfBooker)) {
            throw new NotFoundException("Юзера с таким id " + idOfBooker + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByBookerId(idOfBooker));
        return result;
    }

    private Set<Booking> getAllBookingsByOwnerId(String ownerId) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfOwner = Long.parseLong(ownerId);
        if (!userRepository.existsById(idOfOwner)) {
            throw new NotFoundException("Юзера с таким id " + ownerId + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByItemOwnerId(idOfOwner));
        return result;
    }

    private Booking bookingMaker(Booking booking) {
        Item item = itemRepository.getReferenceById(booking.getItemId());
        User booker = userRepository.getReferenceById(booking.getBookerId());
        booking.setItem(itemMapper.fromItemToDto(item));
        booking.setBooker(userMapper.fromUserToDto(booker));
        bookingRepository.save(booking);
        return booking;
    }
}
