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
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;
    Set<Booking> result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));

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
        long itemOwnerId = itemRepository.getReferenceById(booking.getItemId()).getOwnerId();
        if(itemOwnerId==Long.parseLong(bookerId)) {
            throw new NotFoundException("Нельзя бронировать свою же вещь");
        }
        validateBooking(booking, bookerId);
        booking.setBookerId(Long.parseLong(bookerId));
        booking.setStatus(BookingStatus.WAITING);
        booking.setItemOwnerId(itemRepository.getReferenceById(booking.getItemId()).getOwnerId());
        bookingRepository.save(booking);
        return booking;
    }

    @Override
    public Booking patchBooking(String bookingId, String sharerId, String approved) {
        if (!bookingRepository.existsById(Long.parseLong(bookingId))) {
            throw new NotFoundException("Бронирования с таким id не существует");
        }
        Booking booking = bookingRepository.getReferenceById(Long.parseLong(bookingId));
        Item item = itemRepository.getReferenceById(booking.getItemId());
        User booker = userRepository.getReferenceById(booking.getBookerId());
        if (Long.parseLong(sharerId) != booking.getItemOwnerId()) {
            throw new NotFoundException("Патчить статус вещи может только её владелец");
        }

        if(booking.getStatus()!=BookingStatus.APPROVED) {
            if (approved.equals("true")) {
                booking.setStatus(BookingStatus.APPROVED);
            } else if (approved.equals("false")) {
                booking.setStatus(BookingStatus.REJECTED);
            }
            booking.setItem(getNewItem(item));
            booking.setBooker(getNewBooker(booker));
            bookingRepository.save(booking);
            return booking;
        } else {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        if (!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        if (!bookingRepository.existsById(Long.parseLong(bookingId))) {
            throw new NotFoundException("Букинга с таким id не существует");
        }
        Booking booking = bookingRepository.getReferenceById(Long.parseLong(bookingId));
        if ((booking.getBookerId() == Long.parseLong(ownerOrBooker)) || (booking.getItemOwnerId() == Long.parseLong(ownerOrBooker))) {
            Item item = itemRepository.getReferenceById(booking.getItemId());
            User booker = userRepository.getReferenceById(booking.getBookerId());
            booking.setItem(getNewItem(item));
            booking.setBooker(getNewBooker(booker));
            return booking;
        } else {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем");
        }
    }

    @Override
    public Collection<Booking> getAll(String ownerOrBooker) {
        if (!userRepository.existsById(Long.parseLong(ownerOrBooker))) {
            throw new NotFoundException("Юзера с таким id не существует");
        }
        for (Booking booking : bookingRepository.findAll()) {
            Item item = itemRepository.getReferenceById(booking.getItemId());
            User booker = userRepository.getReferenceById(booking.getBookerId());
            booking.setItem(getNewItem(item));
            booking.setBooker(getNewBooker(booker));
            result.add(booking);
        }
        return result;
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String booker) {
        try {
            BookingState bookingState = BookingState.valueOf(state.substring(6));
            switch (bookingState) {
                case PAST:
                    Collection<Booking> allPastBookings = getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> pastBookings = new ArrayList<>();
                    for(Booking booking: allPastBookings) {
                        LocalDateTime now = LocalDateTime.now();
                        if(booking.getEnd().isBefore(now)) {
                            booking.setState(BookingState.PAST);
                            pastBookings.add(booking);
                        }
                    }
                    return pastBookings;
                case FUTURE:
 //                   return getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> allFutureBookings = getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> futureBookings = new ArrayList<>();
                    for(Booking booking: allFutureBookings) {
                        LocalDateTime now = LocalDateTime.now();
                        if(booking.getStart().isAfter(now)) {
                            booking.setState(BookingState.FUTURE);
                            futureBookings.add(booking);
                        }
                    }
                    return futureBookings;
                case CURRENT:
                   Collection<Booking> allBookings = getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                   Collection<Booking> curBookings = new ArrayList<>();
                   for(Booking booking: allBookings) {
                       LocalDateTime now = LocalDateTime.now();
                       if(booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                           booking.setState(BookingState.CURRENT);
                           curBookings.add(booking);
                       }
                   }
                   return curBookings;
                case WAITING:
                    Collection<Booking> col = bookingRepository.findAll().stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> col2 = col.stream().filter(b -> b.getStatus().equals(BookingStatus.WAITING)).collect(Collectors.toList());
                    col = new ArrayList<>();
                    for (Booking booking : col2) {
                        Item item = getNewItem(itemRepository.getReferenceById(booking.getItemId()));
                        User innerBooker = getNewBooker(userRepository.getReferenceById(booking.getBookerId()));
                        booking.setItem(item);
                        booking.setBooker(innerBooker);
                        col.add(booking);
                        bookingRepository.save(booking);
                    }
                    return col;
                case REJECTED:
                    Collection<Booking> col3 = bookingRepository.findAll().stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> col4 = col3.stream().filter(b -> b.getStatus().equals(BookingStatus.REJECTED)).collect(Collectors.toList());
                    col3 = new ArrayList<>();
                    for (Booking booking : col4) {
                        Item item = getNewItem(itemRepository.getReferenceById(booking.getItemId()));
                        User innerBooker = getNewBooker(userRepository.getReferenceById(booking.getBookerId()));
                        booking.setItem(item);
                        booking.setBooker(innerBooker);
                        col3.add(booking);
                        bookingRepository.save(booking);
                    }
                    return col3;
            }
        } catch (Exception e) {
            throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
        }
        return getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
    }

    @Override
    public Collection<Booking> getAllForCurrentUser(String state, String curUser) {
        if (!userRepository.existsById(Long.parseLong(curUser))) {
            throw new NotFoundException("Юзера с таким айди нет в нашей базе");
        }
        result = getAllBookingByOwnerId(curUser);
        for (Booking booking : result) {
            Item item = itemRepository.getReferenceById(booking.getItemId());
            User booker = userRepository.getReferenceById(booking.getBookerId());
            booking.setItem(getNewItem(item));
            booking.setBooker(getNewBooker(booker));
        }
        if (state!=null) {
            try {
                BookingState bookingState = BookingState.valueOf(state.substring(6));
                switch (bookingState) {
                    case PAST:
                        Collection<Booking> allPastBookings = getAll(curUser).stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                        Collection<Booking> pastBookings = new ArrayList<>();
                        for(Booking booking: allPastBookings) {
                            LocalDateTime now = LocalDateTime.now();
                            if(booking.getEnd().isBefore(now)) {
                                booking.setState(BookingState.PAST);
                                pastBookings.add(booking);
                            }
                        }
                        return pastBookings;
                    case FUTURE:
//                        return getAll(curUser);
                    Collection<Booking> allFutureBookings = getAll(curUser).stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                    Collection<Booking> futureBookings = new ArrayList<>();
                    for(Booking booking: allFutureBookings) {
                        LocalDateTime now = LocalDateTime.now();
                        if(booking.getStart().isAfter(now)) {
                            booking.setState(BookingState.FUTURE);
                            futureBookings.add(booking);
                        }
                    }
                    return futureBookings;
                    case CURRENT:
                        Collection<Booking> allBookings = getAll(curUser).stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                        Collection<Booking> curBookings = new ArrayList<>();
                        for(Booking booking: allBookings) {
                            LocalDateTime now = LocalDateTime.now();
                            if(booking.getStart().isBefore(now) && (booking.getEnd().isAfter(now))) {
                                booking.setState(BookingState.CURRENT);
                                curBookings.add(booking);
                            }
                        }
                        return curBookings;
                    case WAITING:
                        Collection<Booking> col = bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                        Collection<Booking> col2 = col.stream().filter(b -> b.getStatus().equals(BookingStatus.WAITING)).collect(Collectors.toList());
                        col = new ArrayList<>();
                        for (Booking booking : col2) {
                            Item item = getNewItem(itemRepository.getReferenceById(booking.getItemId()));
                            User innerBooker = getNewBooker(userRepository.getReferenceById(booking.getBookerId()));
                            booking.setItem(item);
                            booking.setBooker(innerBooker);
                            col.add(booking);
                            bookingRepository.save(booking);
                        }
                        return col;
                    case REJECTED:
                        Collection<Booking> col3 = bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                        Collection<Booking> col4 = col3.stream().filter(b -> b.getStatus().equals(BookingStatus.REJECTED)).collect(Collectors.toList());
                        col3 = new ArrayList<>();
                        for (Booking booking : col4) {
                            Item item = getNewItem(itemRepository.getReferenceById(booking.getItemId()));
                            User innerBooker = getNewBooker(userRepository.getReferenceById(booking.getBookerId()));
                            booking.setItem(item);
                            booking.setBooker(innerBooker);
                            col3.add(booking);
                            bookingRepository.save(booking);
                        }
                        return col3;
                }
            } catch (Exception e) {
                throw new ValidationException("Unknown state: UNSUPPORTED_STATUS");
            }
        }
        return result;
    }

//----------------------------всякая вспомогательная фигня--------------------------------------------------------------

    private void validateBooking(Booking booking, String bookerId) {
        if (!userRepository.existsById(Long.parseLong(bookerId))) {
            throw new NotFoundException("Букера с таким id не существует");
        }
        if (!itemRepository.existsById(booking.getItemId())) {
            throw new NotFoundException("Нет айтема с таким id");
        }
        if (!itemRepository.getReferenceById(booking.getItemId()).getAvailable()) {
            throw new ValidationException("Недоступный для бронирования");
        }
        if ((booking.getEnd().isBefore(booking.getStart()))
                || (booking.getEnd().isBefore(LocalDateTime.now()))
                || (booking.getStart().isBefore(LocalDateTime.now()))) {
            throw new ValidationException("Время аренды не может быть в прошлом");
        }
    }

    private Set<Booking> getAllBookingByOwnerId(String ownerId) {
        result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        result.addAll(bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(ownerId)).collect(Collectors.toSet()));
        return result;
    }

    private Set<Booking> getAllBookingsByBookerId(String bookerId) {
        result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
        result.addAll(bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(bookerId)).collect(Collectors.toSet()));
        return result;
    }

    private Booking getBookingByOwnerAndId(String ownerId, String bookingId) {
        Booking booking = bookingRepository.getReferenceById(Long.parseLong(bookingId));
        if (booking.getItemOwnerId() == Long.parseLong(ownerId)) {
            return booking;
        }
        return null;
    }

    private Booking getBookingByBookerAndId(String bookerId, String bookingId) {
        Booking booking = bookingRepository.getReferenceById(Long.parseLong(bookingId));
        if (booking.getItemOwnerId() == Long.parseLong(bookerId)) {
            return booking;
        }
        return null;
    }

    private Item getNewItem(Item item) {
        Item newItem = new Item();
        newItem.setId(item.getId());
        newItem.setName(item.getName());
        newItem.setDescription(item.getDescription());
        newItem.setAvailable(item.getAvailable());
        newItem.setOwnerId(item.getOwnerId());
        return newItem;
    }

    private User getNewBooker(User booker) {
        User newBooker = new User();
        newBooker.setId(booker.getId());
        newBooker.setName(booker.getName());
        newBooker.setEmail(booker.getEmail());
        return newBooker;
    }
}
