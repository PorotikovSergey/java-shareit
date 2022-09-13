package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
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
    public Booking postBooking(Booking booking, String bookerid, long itemId) {
        long bookerId = Long.parseLong(bookerid);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        User booker = userRepository.findById(bookerId).get();
        validateBooking(booking, bookerid, itemId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setItemOwner(item.getOwner());
        bookingRepository.save(booking);
        return booking;
    }

    @Override
    public Booking patchBooking(String parsedBookingId, String parsedOwnerId, boolean approved) {
        long idOfBooking = Long.parseLong(parsedBookingId);
        long idOfOwner = Long.parseLong(parsedOwnerId);

        checkBooking(idOfBooking);

        Booking booking = bookingRepository.findById(idOfBooking).get();
        checkAccessForPatchBooking(idOfOwner, booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return booking;
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        long idOfBooking = Long.parseLong(bookingId);
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);
        checkUser(idOfOwnerOrBooker);
        checkBooking(idOfBooking);

        Booking booking = bookingRepository.findById(idOfBooking).get();
        checkAccessForGetBooking(booking, idOfOwnerOrBooker);

        return booking;
    }

    @Override
    public List<Booking> getAllForBooker(String state, String first, String size, String booker) {
        List<Booking> list = getAllBookingsForBooker(booker);
        return checkUserAndStateAndPageAndReturnResultList(list, state, first, size, booker);
    }

    @Override
    public List<Booking> getAllForOwner(String state, String first, String size, String owner) {
        List<Booking> list = getAllBookingsByOwner(owner);
        return checkUserAndStateAndPageAndReturnResultList(list, state, first, size, owner);
    }

//------------------------------private-----------------------------------------------------------------

    private void checkAccessForGetBooking(Booking booking, long idOfOwnerOrBooker) {
        if (!((booking.getBooker().getId() == idOfOwnerOrBooker) || (booking.getItemOwner().getId() == idOfOwnerOrBooker))) {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем. " +
                    "Айди владельца " + booking.getItemOwner().getId() + ". " +
                    "Айди арендатора " + booking.getBooker().getId() + ". " +
                    "Айди желающего " + idOfOwnerOrBooker);
        }
    }

    private void checkAccessForPatchBooking(long idOfOwner, Booking booking) {
        if (idOfOwner != booking.getItemOwner().getId()) {
            throw new NotFoundException("Патчить статус вещи может владелец, а не пользователь с айди " + idOfOwner);
        }

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }
    }

//    private void checkAccessForBookingOwnItem(long ownerId, long itemBookerId) {
//        if (ownerId == itemBookerId) {
//            throw new NotFoundException("Нельзя бронировать свою же вещь. " +
//                    "Айди владельца " + ownerId + ". Айди  желающего " + itemBookerId);
//        }
//    }

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

    private void validateBooking(Booking booking, String bookerId, long itemId) {
        checkUser(Long.parseLong(bookerId));
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Вещи с таким айди " + itemId + "не существует");
        }
        if (!itemRepository.findById(itemId).get().getAvailable()) {
            throw new ValidationException("Недоступный для бронирования. Айди вещи " + itemId);
        }

        if(Long.parseLong(bookerId) == itemRepository.findById(itemId).get().getId()) {
            throw new NotFoundException("Нельзя арендовать свою вещь у себя же самого");
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
        return new ArrayList<>(result);
    }

    private List<Booking> getAllBookingsByOwner(String owner) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        long idOfOwner = Long.parseLong(owner);
        if (!userRepository.existsById(idOfOwner)) {
            throw new NotFoundException("Юзера с таким id " + owner + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByItemOwnerId(idOfOwner));
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
//        booking.setItem(itemRepository.findById(booking.getItem().getId()).get());
//        booking.setBooker(userRepository.findById(booking.getItem().getId()).get());
//        bookingRepository.save(booking);
        return booking;
    }
}
