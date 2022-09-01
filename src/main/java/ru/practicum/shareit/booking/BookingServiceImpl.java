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
        Item item = itemRepository.getReferenceById(booking.getItemId());
        long itemBookerId = Long.parseLong(bookerId);
        User booker = userRepository.getReferenceById(itemBookerId);

        if(itemOwnerId==itemBookerId) {
            throw new NotFoundException("Нельзя бронировать свою же вещь");
        }

        validateBooking(booking, bookerId);
        booking.setBookerId(itemBookerId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItemOwnerId(itemOwnerId);
        return weakBookingMaker(booking, item, booker);
    }

    @Override
    public Booking patchBooking(String bookingId, String sharerId, String approved) {
        long idOfBooking = Long.parseLong(bookingId);
        long idOfOwner = Long.parseLong(sharerId);

        if (!bookingRepository.existsById(idOfBooking)) {
            throw new NotFoundException("Бронирования с таким id не существует");
        }

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if (idOfOwner != booking.getItemOwnerId()) {
            throw new NotFoundException("Патчить статус вещи может только её владелец");
        }

        if(booking.getStatus()!=BookingStatus.APPROVED) {
            if (approved.equals("true")) {
                booking.setStatus(BookingStatus.APPROVED);
            } else if (approved.equals("false")) {
                booking.setStatus(BookingStatus.REJECTED);
            }
            return strongBookingMaker(booking);
        } else {
            throw new ValidationException("Нельзя менять статус уже подтверждённой брони");
        }
    }

    @Override
    public Booking getBooking(String ownerOrBooker, String bookingId) {
        long idOfBooking =  Long.parseLong(bookingId);
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);

        if (!userRepository.existsById(idOfOwnerOrBooker)) {
            throw new NotFoundException("Юзера с таким id не существует");
        }

        if (!bookingRepository.existsById(idOfBooking)) {
            throw new NotFoundException("Букинга с таким id не существует");
        }

        Booking booking = bookingRepository.getReferenceById(idOfBooking);

        if ((booking.getBookerId() == idOfOwnerOrBooker) || (booking.getItemOwnerId() == idOfOwnerOrBooker)) {
            return strongBookingMaker(booking);
        } else {
            throw new NotFoundException("Только владелец или арендатор могут просматривать айтем");
        }
    }

    @Override
    public Collection<Booking> getAll(String ownerOrBooker) {
        long idOfOwnerOrBooker = Long.parseLong(ownerOrBooker);

        if (!userRepository.existsById(idOfOwnerOrBooker)) {
            throw new NotFoundException("Юзера с таким id не существует");
        }

        for (Booking booking : bookingRepository.findAll()) {
            result.add(strongBookingMaker(booking));
        }
        return result;
    }

    @Override
    public Collection<Booking> getAllByBooker(String state, String booker) {
        if(state==null) {
            System.out.println("стейт равен нулл");
            return getAll(booker).stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
        }
        long idOfBooker = Long.parseLong(booker);
        try {
            BookingState bookingState = BookingState.valueOf(state.substring(6));
            switch (bookingState) {
                case PAST:
                    //Collection<Booking> allPastBookings = getAll(booker).stream().filter(b -> b.getBookerId() == idOfBooker).collect(Collectors.toList());
                    Collection<Booking> allPastBookings = bookingRepository.findByBookerId(idOfBooker).stream().map(this::strongBookingMaker).collect(Collectors.toList());
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
                        col.add(strongBookingMaker(booking));
                    }
                    return col;
                case REJECTED:
                    Collection<Booking> col3 = bookingRepository.findAll().stream().filter(b -> b.getBookerId() == Long.parseLong(booker)).collect(Collectors.toList());
                    Collection<Booking> col4 = col3.stream().filter(b -> b.getStatus().equals(BookingStatus.REJECTED)).collect(Collectors.toList());
                    col3 = new ArrayList<>();
                    for (Booking booking : col4) {
                        col3.add(strongBookingMaker(booking));
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
            strongBookingMaker(booking);
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
                            col.add(strongBookingMaker(booking));
                        }
                        return col;
                    case REJECTED:
                        Collection<Booking> col3 = bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(curUser)).collect(Collectors.toList());
                        Collection<Booking> col4 = col3.stream().filter(b -> b.getStatus().equals(BookingStatus.REJECTED)).collect(Collectors.toList());
                        col3 = new ArrayList<>();
                        for (Booking booking : col4) {
                            col3.add(strongBookingMaker(booking));
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
        result.clear();
//        result = new TreeSet<>((o1, o2) -> (o2.getStart().compareTo(o1.getStart())));
//        Set<Booking> resultSet = bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(ownerId)).collect(Collectors.toSet());
        result.addAll(bookingRepository.findAll().stream().filter(b -> b.getItemOwnerId() == Long.parseLong(ownerId)).collect(Collectors.toSet()));
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
