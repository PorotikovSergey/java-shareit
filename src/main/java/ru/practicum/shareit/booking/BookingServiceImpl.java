package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
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
        long ownerId = itemRepository.findById(booking.getItemId()).get().getOwnerId();
        long itemBookerId = Long.parseLong(bookerId);

        checkAccessForBookingOwnItem(ownerId, itemBookerId);

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

        Booking booking = bookingRepository.findById(idOfBooking).get();
        checkAccessForPatchBooking(idOfOwner, booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMaker(booking);
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        long idOfBooking = Long.parseLong(bookingId);
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);
        checkUser(idOfOwnerOrBooker);
        checkBooking(idOfBooking);

        Booking booking = bookingRepository.findById(idOfBooking).get();
        checkAccessForGetBooking(booking, idOfOwnerOrBooker);

        return bookingMaker(booking);
    }

    @Override
    public List<Booking> getAllForBooker(String state, String first, String size, String booker) {
        return checkUserAndStateAndPageAndReturnResultList(getAllBookingsForBooker(booker), state, first, size, booker);
    }

    @Override
    public List<Booking> getAllForOwner(String state, String first, String size, String owner) {
        return checkUserAndStateAndPageAndReturnResultList(getAllBookingsByOwner(owner), state, first, size, owner);
    }

//------------------------------private-----------------------------------------------------------------

    private void checkAccessForGetBooking(Booking booking, long idOfOwnerOrBooker) {
        if (!((booking.getBookerId() == idOfOwnerOrBooker) || (booking.getItemOwnerId() == idOfOwnerOrBooker))) {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем. " +
                    "Айди владельца " + booking.getItemOwnerId() + ". " +
                    "Айди арендатора " + booking.getBookerId() + ". " +
                    "Айди желающего " + idOfOwnerOrBooker);
        }
    }

    private void checkAccessForPatchBooking(long idOfOwner, Booking booking) {
        if (idOfOwner != booking.getItemOwnerId()) {
            throw new NotFoundException("Патчить статус вещи может владелец, а не пользователь с айди " + idOfOwner);
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }
    }

    private void checkAccessForBookingOwnItem(long ownerId, long itemBookerId) {
        if (ownerId == itemBookerId) {
            throw new NotFoundException("Нельзя бронировать свою же вещь. " +
                    "Айди владельца " + ownerId + ". Айди  желающего " + itemBookerId);
        }
    }

    private List<Booking> checkUserAndStateAndPageAndReturnResultList(List<Booking> bookings, String state,
                                                                      String first, String size, String user) {
        long bookerId = Long.parseLong(user);
        checkUser(bookerId);

        if ((state == null) && (first == null)) {
            return bookings;
        }

        if (first != null) {
            int firstEl = Integer.parseInt(first);
            int sizePage = Integer.parseInt(size);
            if ((firstEl < 1) || (sizePage < 1)) {
                throw new ValidationException("Невалидные значения from и size");
            }
            return getPageableList(new ArrayList<>(bookings), firstEl, sizePage);
        }
        return getRightStateList(bookings, getStateOfString(state));
    }

    private BookingState getStateOfString(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private List<Booking> getPageableList(List<Booking> bookings, int firstEl, int sizePage) {
        PagedListHolder<Booking> page = new PagedListHolder<>(bookings.subList(firstEl, bookings.size()));
        page.setPageSize(sizePage);
        page.setPage(0);
        List<Booking> result = new ArrayList<>();
        for (Booking booking : page.getPageList()) {
            result.add(bookingMaker(booking));
        }
        return result;
    }

    private void validateBooking(Booking booking, String bookerId) {
        checkUser(Long.parseLong(bookerId));
        checkItem(booking.getItemId());
        if (!itemRepository.findById(booking.getItemId()).get().getAvailable()) {
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
            throw new NotFoundException("Айтема с таким айди " + itemId + " отсутствует");
        }
    }

    private void checkBooking(long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Букинга с таким айди " + bookingId + " нет");
        }
    }

    private List<Booking> getAllBookingsForBooker(String booker) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfBooker = Long.parseLong(booker);
        if (!userRepository.existsById(idOfBooker)) {
            throw new NotFoundException("Юзера с таким id " + idOfBooker + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByBookerId(idOfBooker));
        result.forEach(this::bookingMaker);
        return new ArrayList<>(result);
    }

    private List<Booking> getAllBookingsByOwner(String owner) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfOwner = Long.parseLong(owner);
        if (!userRepository.existsById(idOfOwner)) {
            throw new NotFoundException("Юзера с таким id " + owner + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByItemOwnerId(idOfOwner));
        result.forEach(this::bookingMaker);
        return new ArrayList<>(result);
    }

    private List<Booking> getRightStateList(List<Booking> before, BookingState state) {
        List<Booking> result = new ArrayList<>();
        switch (state) {
            case PAST:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getEnd().isBefore(now)) {
                        booking.setState(BookingState.PAST);
                        result.add(booking);
                    }
                }
                break;
            case FUTURE:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isAfter(now)) {
                        booking.setState(BookingState.FUTURE);
                        result.add(booking);
                    }
                }
                break;
            case CURRENT:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                        booking.setState(BookingState.CURRENT);
                        result.add(booking);
                    }
                }
                break;
            case WAITING:
                Collection<Booking> waitingBookings = before.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                        .collect(Collectors.toList());
                for (Booking booking : waitingBookings) {
                    result.add(bookingMaker(booking));
                }
                break;
            case REJECTED:
                Collection<Booking> rejectedBookings = before.stream()
                        .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                        .collect(Collectors.toList());
                for (Booking booking : rejectedBookings) {
                    result.add(bookingMaker(booking));
                }
                break;
            default:
                result = new ArrayList<>(before);
        }
        return result;
    }

    private Booking bookingMaker(Booking booking) {
        booking.setItem(itemRepository.findById(booking.getItemId()).get());
        booking.setBooker(userRepository.findById(booking.getBookerId()).get());
        bookingRepository.save(booking);
        return booking;
    }
}
