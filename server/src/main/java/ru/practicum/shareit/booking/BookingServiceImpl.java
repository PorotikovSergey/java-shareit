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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl {
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

    public Booking postBooking(Booking booking, long bookerId, long itemId) throws ValidationException {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        User booker = userRepository.findById(bookerId).orElseThrow(() -> new NotFoundException("Такого юзера нет"));
        if (!booking.getStart().isBefore(booking.getEnd())) {
            throw new ValidationException("Конец брони не должен быть раньше начала");
        }
        validateBooking(bookerId, itemId);
        booking.setStatus("WAITING");
        booking.setItem(item);
        booking.setBooker(booker);
        bookingRepository.save(booking);
        return booking;
    }

    public Booking patchBooking(long bookingId, long userId, boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Такого букинга нет"));
        checkAccessForPatchBooking(userId, booking);

        booking.setStatus(approved ? "APPROVED" : "REJECTED");
        bookingRepository.save(booking);
        return booking;
    }

    public Booking getBooking(long userId, long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Такого букинга нет"));
        checkAccessForGetBooking(booking, userId);

        return booking;
    }

    public List<Booking> getAllForBooker(String state, int first, int size, long bookerId) {
        List<Booking> list = getAllBookingsForBooker(bookerId);
        return checkStateAndPageAndReturnResultList(list, state, first, size);
    }

    public List<Booking> getAllForOwner(String state, int first, int size, long ownerId) {
        List<Booking> list = getAllBookingsByOwner(ownerId);
        return checkStateAndPageAndReturnResultList(list, state, first, size);
    }


    private void checkAccessForGetBooking(Booking booking, long idOfOwnerOrBooker) {
        if (!((booking.getBooker().getId() == idOfOwnerOrBooker) || (booking.getItem().getOwner().getId() == idOfOwnerOrBooker))) {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем");
        }
    }

    private void checkAccessForPatchBooking(long idOfOwner, Booking booking) {
        if (idOfOwner != booking.getItem().getOwner().getId()) {
            throw new NotFoundException("Патчить статус вещи может владелец, а не пользователь с айди " + idOfOwner);
        }

        if (booking.getStatus().equals("APPROVED")) {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }
    }

    private List<Booking> checkStateAndPageAndReturnResultList(List<Booking> bookings, String state,
                                                               int first, int size) {
        if ((state == null) && (first == 0)) {
            return bookings;
        }
        if (first != 0) {
            return getPageableList(new ArrayList<>(bookings), first, size);
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
        return page.getPageList();
    }

    private void validateBooking(long bookerId, long itemId) {
        if (!itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого айтема нет"))
                .getAvailable()) {
            throw new ValidationException("Недоступный для бронирования. Айди вещи " + itemId);
        }

        if (bookerId == itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Такого айтема нет"))
                .getId()) {
            throw new NotFoundException("Нельзя арендовать свою вещь у себя же самого");
        }
    }

    public List<Booking> getAllBookingsForBooker(long bookerId) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Юзера с таким id " + bookerId + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByBookerId(bookerId));
        return new ArrayList<>(result);
    }

    private List<Booking> getAllBookingsByOwner(long ownerId) {
        Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Юзера с таким id " + ownerId + " не существует");
        }
        result.addAll(bookingRepository.findBookingsByItemOwnerId(ownerId));
        return new ArrayList<>(result);
    }

    private List<Booking> getRightStateList(List<Booking> before, BookingState state) {
        List<Booking> result = new ArrayList<>();
        switch (state) {
            case PAST:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getEnd().isBefore(now)) {
                        result.add(booking);
                    }
                }
                break;
            case FUTURE:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isAfter(now)) {
                        result.add(booking);
                    }
                }
                break;
            case CURRENT:
                for (Booking booking : before) {
                    LocalDateTime now = LocalDateTime.now();
                    if (booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                        result.add(booking);
                    }
                }
                break;
            case WAITING:
                result = before.stream()
                        .filter(b -> b.getStatus().equals("WAITING"))
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                result = before.stream()
                        .filter(b -> b.getStatus().equals("REJECTED"))
                        .collect(Collectors.toList());
                break;
            default:
                result = new ArrayList<>(before);
        }
        return result;
    }
}
