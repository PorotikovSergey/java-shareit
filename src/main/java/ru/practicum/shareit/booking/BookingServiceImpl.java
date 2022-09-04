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
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking postBooking(Booking booking, String bookerId) {
        checkItem(booking.getItemId());
        long ownerId = itemRepository.getReferenceById(booking.getItemId()).getOwnerId();
        Item item = itemRepository.getReferenceById(booking.getItemId());
        long itemBookerId = Long.parseLong(bookerId);
        User booker = userRepository.getReferenceById(itemBookerId);

        if (ownerId == itemBookerId) {
            throw new NotFoundException("Нельзя бронировать свою же вещь");
        }

        validateBooking(booking, bookerId);
        booking.setBookerId(itemBookerId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItemOwnerId(ownerId);
        return weakBookingMaker(booking, item, booker);
    }

    @Override
    public Booking patchBooking(String parsedBookingId, String parsedOwnerId, String approved) {
        long idOfBooking = Long.parseLong(parsedBookingId);
        long idOfOwner = Long.parseLong(parsedOwnerId);

        checkBooking(idOfBooking);

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if (idOfOwner != booking.getItemOwnerId()) {
            throw new NotFoundException("Патчить статус вещи может только её владелец");
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }

        if (approved.equals("true")) {
            booking.setStatus(BookingStatus.APPROVED);
        } else if (approved.equals("false")) {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return strongBookingMaker(booking);
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        long idOfBooking = Long.parseLong(bookingId);
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);

        checkUser(idOfOwnerOrBooker);

        checkBooking(idOfBooking);

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if ((booking.getBookerId() == idOfOwnerOrBooker) || (booking.getItemOwnerId() == idOfOwnerOrBooker)) {
            return strongBookingMaker(booking);
        } else {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем");
        }
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String booker) {
        Collection<Booking> allBookings = getAllBookingsForBooker(booker);

        for (Booking booking : allBookings) {
            strongBookingMaker(booking);
        }
        if (state == null) {
            return allBookings;
        }

        try {
            BookingState bookingState = BookingState.valueOf(state.substring(6));
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
                        resultWaitingList.add(strongBookingMaker(booking));
                    }
                    return resultWaitingList;
                case REJECTED:
                    Collection<Booking> allRejectedBookings = allBookings.stream()
                            .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                            .collect(Collectors.toList());
                    List<Booking> resultRejectedBookings = new ArrayList<>();
                    for (Booking booking : allRejectedBookings) {
                        resultRejectedBookings.add(strongBookingMaker(booking));
                    }
                    return resultRejectedBookings;
            }
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return allBookings;
    }

    @Override
    public Collection<Booking> getAllForUser(String state, String user) {
        Collection<Booking> allBookings = getAllBookingsByOwnerId(user);

        for (Booking booking : allBookings) {
            strongBookingMaker(booking);
        }

        if (state == null) {
            return allBookings;
        }

        try {
            BookingState bookingState = BookingState.valueOf(state.substring(6));
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
                        resultWaitingBookings.add(strongBookingMaker(booking));
                    }
                    return resultWaitingBookings;
                case REJECTED:
                    Collection<Booking> rejectedBookings = allBookings.stream()
                            .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                            .collect(Collectors.toList());
                    Collection<Booking> resultRejectedBookings = new ArrayList<>();
                    for (Booking booking : rejectedBookings) {
                        resultRejectedBookings.add(strongBookingMaker(booking));
                    }
                    return resultRejectedBookings;
            }
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return allBookings;
    }

//----------------------------всякая вспомогательная фигня--------------------------------------------------------------

    private void validateBooking(Booking booking, String bookerId) {
        checkUser(Long.parseLong(bookerId));
        checkItem(booking.getItemId());
        if (!itemRepository.getReferenceById(booking.getItemId()).getAvailable()) {
            throw new ValidationException("Недоступный для бронирования");
        }
        if ((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
    }

    private void checkUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким айди нет в нашей базе");
        }
    }

    private void checkItem(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Айтема с таким айди нет в нашей базе");
        }
    }

    private void checkBooking(long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Букинга с таким айди нет в нашей базе");
        }
    }

    private Collection<Booking> getAllBookingsForBooker(String booker) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfBooker = Long.parseLong(booker);
        if (!userRepository.existsById(idOfBooker)) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        result.addAll(bookingRepository.findBookingsByBookerId(idOfBooker));
        return result;
    }

    private Set<Booking> getAllBookingsByOwnerId(String ownerId) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfOwner = Long.parseLong(ownerId);
        if (!userRepository.existsById(idOfOwner)) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        result.addAll(bookingRepository.findBookingsByItemOwnerId(idOfOwner));
        return result;
    }

    private Item getItemForBooking(Item item) {
        Item newItem = new Item();
        newItem.setId(item.getId());
        newItem.setName(item.getName());
        newItem.setDescription(item.getDescription());
        newItem.setAvailable(item.getAvailable());
        newItem.setOwnerId(item.getOwnerId());
        return newItem;
    }

    private User getBookerForBooking(User booker) {
        User newBooker = new User();
        newBooker.setId(booker.getId());
        newBooker.setName(booker.getName());
        newBooker.setEmail(booker.getEmail());
        return newBooker;
    }

    private Booking weakBookingMaker(Booking booking, Item item, User booker) {
        booking.setItem(getItemForBooking(item));
        booking.setBooker(getBookerForBooking(booker));
        bookingRepository.save(booking);
        return booking;
    }

    private Booking strongBookingMaker(Booking booking) {
        Item item = itemRepository.getReferenceById(booking.getItemId());
        User booker = userRepository.getReferenceById(booking.getBookerId());
        return weakBookingMaker(booking, item, booker);
    }


}
