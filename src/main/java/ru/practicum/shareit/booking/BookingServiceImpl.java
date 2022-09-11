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

        Booking booking = bookingRepository.findById(idOfBooking).get();

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

        Booking booking = bookingRepository.findById(idOfBooking).get();

        if (!((booking.getBookerId() == idOfOwnerOrBooker) || (booking.getItemOwnerId() == idOfOwnerOrBooker))) {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем. " +
                    "Айди владельца " + booking.getItemOwnerId() + ". " +
                    "Айди арендатора " + booking.getBookerId() + ". " +
                    "Айди желающего " + idOfOwnerOrBooker);
        }

        return bookingMaker(booking);
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String first, String size, String booker) {
        long bookerId = Long.parseLong(booker);
        checkUser(bookerId);

        BookingState bookingState;
        Collection<Booking> allBookings = getAllBookingsForBooker(booker);

        for (Booking booking : allBookings) {
            bookingMaker(booking);
        }

        if ((state == null) && (first == null)) {
            return allBookings;
        }

        if((state == null) && (first!=null)) {
            int firstEl = Integer.parseInt(first);
            int sizePage = Integer.parseInt(size);
            if ((firstEl < 1) || (sizePage < 1)) {
                throw new ValidationException("Невалидные значения from и size");
            }
            return getPageableList(new ArrayList<>(allBookings), firstEl, sizePage);
        }

        try {
            bookingState = BookingState.valueOf(state);
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
    public Collection<Booking> getAllForUser(String state,  String first, String size, String user) {
        long userId = Long.parseLong(user);
        checkUser(userId);

        BookingState bookingState;
        Collection<Booking> allBookings = getAllBookingsByOwnerId(user);
        for (Booking booking : allBookings) {
            bookingMaker(booking);
        }

        if ((state == null) && (first == null)) {
            return allBookings;
        }

        if((state == null) && (first!=null)) {
            int firstEl = Integer.parseInt(first);
            int sizePage = Integer.parseInt(size);
            if ((firstEl < 1) || (sizePage < 1)) {
                throw new ValidationException("Невалидные значения from и size");
            }
            return getPageableList(new ArrayList<>(allBookings), firstEl, sizePage);
        }

        try {
            bookingState = BookingState.valueOf(state);
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


    private Collection<Booking> getAllForUserPageable(String from, String size, String user) {
        long userId = Long.parseLong(user);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким айди " + user + " нет");
        }
        int firstEl = Integer.parseInt(from);
        int sizePage = Integer.parseInt(size);
        if ((firstEl < 1) || (sizePage < 1)) {
            throw new ValidationException("Невалидные значения from и size");
        }

        List<Booking> allBookings = new ArrayList<>(getAllBookingsByOwnerId(user));
//        PagedListHolder page = new PagedListHolder(new ArrayList<>(allBookings.subList(firstEl, allBookings.size())));
//        page.setPageSize(sizePage);
//        page.setPage(0);
//        List<Booking> result = new ArrayList<>();
//        for(Object booking: page.getPageList()) {
//            result.add(bookingMaker((Booking) booking));
//        }
//        return result;
        return getPageableList(allBookings, firstEl, sizePage);
    }

    private List<Booking> getPageableList(List<Booking> bookingssss, int firstEl, int sizePage) {
        List<Booking> bookings = new ArrayList<>(bookingssss);
        PagedListHolder page = new PagedListHolder(new ArrayList<>(bookings.subList(firstEl, bookings.size())));
        page.setPageSize(sizePage);
        page.setPage(0);
        List<Booking> result = new ArrayList<>();
        for(Object booking: page.getPageList()) {
            result.add(bookingMaker((Booking) booking));
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
            throw new NotFoundException("Айтема с таким айди " + itemId + " нет в нашей базе");
        }
    }

    private void checkBooking(long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Букинга с таким айди " + bookingId + " нет в нашей базе");
        }
    }

    private Collection<Booking> getAllForBookerPageable(String first, String size, String booker) {
        long bookerId = Long.parseLong(booker);
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("Юзера с таким айди " + bookerId + " нет");
        }
        int firstEl = Integer.parseInt(first);
        int sizePage = Integer.parseInt(size);

        if ((firstEl < 1) || (sizePage < 1)) {
            throw new ValidationException("Невалидные значения from и size");
        }

        List<Booking> allBookings =  new ArrayList<>(getAllBookingsForBooker(booker));
        PagedListHolder page = new PagedListHolder(new ArrayList<>(allBookings.subList(firstEl, allBookings.size())));
        page.setPageSize(sizePage);
        page.setPage(0);
        List<Booking> result = new ArrayList<>();
        for(Object booking: page.getPageList()) {
            result.add(bookingMaker((Booking) booking));
        }
        return result;
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
        booking.setItem(itemRepository.findById(booking.getItemId()).get());
        booking.setBooker(userRepository.findById(booking.getBookerId()).get());
        bookingRepository.save(booking);
        return booking;
    }
}
