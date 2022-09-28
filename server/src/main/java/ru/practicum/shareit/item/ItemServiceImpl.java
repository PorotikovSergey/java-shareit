package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServiceException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Item> getAll(long ownerId, int from, int size) {
        List<Item> allItemsByOwner = itemRepository.findAllByOwnerId(ownerId);
        return getPageable(itemsWithStartAndEnd(allItemsByOwner, ownerId), from, size, ownerId);

    }

    @Override
    public Item postItem(Item item, long ownerId) {
        item.setOwner(userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Такого юзера нет")));
        itemRepository.save(item);
        return item;
    }

    @Override
    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public Item patchItem(long itemId, Item item, long ownerId) {
        validateItemForPatch(ownerId, itemId);
        Item forPatch = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        patchOneItemFromAnother(item, forPatch);
        itemRepository.save(forPatch);
        return forPatch;
    }

    @Override
    public Item getItem(long userId, long itemId) {
        List<Booking> bookingsByUser = bookingRepository.findAllByBookerIdOrItemOwnerId(userId, userId);
        Item resultItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        if (userId == resultItem.getOwner().getId()) {
            resultItem.setNextBooking(getNextBooking(bookingsByUser));
            resultItem.setLastBooking(getLastBooking(bookingsByUser));
        }
        resultItem.setComments(commentRepository.findAllByItemId(itemId));
        return resultItem;
    }

    @Override
    public List<Item> searchItem(String text, long ownerId, int from, int size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> foundNames = itemRepository
                .findAllByNameContainingIgnoreCaseAndAvailableIs(text, true);
        List<Item> foundDescriptions = itemRepository
                .findAllByDescriptionContainingIgnoreCaseAndAvailableIs(text, true);
        foundNames.removeAll(foundDescriptions);
        foundNames.addAll(foundDescriptions);
        if (from != 0) {
            return getPageable(foundNames, from, size, ownerId);
        }
        return foundNames;
    }

    @Override
    public Comment postComment(long bookerId, long itemId, Comment comment) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        Booking booking = bookingRepository.findAllByItemId(itemId).stream()
                .filter(b -> b.getBooker().getId() == bookerId)
                .findFirst()
                .orElse(null);

        checkCommentBeforePosting(booking);
        comment.setItem(item);
        comment.setAuthorName(userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет"))
                .getName());
        commentRepository.save(comment);
        return comment;
    }

    @Override
    public Item postItemToRequest(Item item, long ownerId, long requestId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет"));
        item.setOwner(owner);
        item.setRequestId(requestId);
        itemRepository.save(item);
        return item;
    }

    private List<Item> getPageable(List<Item> items, int firstEl, int sizePage, long userId) {
        PagedListHolder<Item> page = new PagedListHolder<>(new ArrayList<>(items.subList(firstEl, items.size())));
        page.setPageSize(sizePage);
        page.setPage(0);
        return itemsWithStartAndEnd(page.getPageList(), userId);
    }

    public void checkCommentBeforePosting(Booking booking) {
        if (booking == null) {
            throw new NotFoundException("Бронирования на данный айтем не было");
        }
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Отзывы возможны только к прошедшим броням");
        }
    }

    private void validateItemForPatch(long ownerId, long id) {
        if (ownerId == 0) {
            throw new ServiceException("Отсутствует владелец");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        if (ownerId != item.getOwner().getId()) {
            throw new NotFoundException("Патчить вещь может только её владелец с айди "
                    + item.getOwner().getId());
        }
    }

    private Item patchOneItemFromAnother(Item donor, Item recipient) {
        if (donor.getName() != null) {
            recipient.setName(donor.getName());
        }
        if (donor.getDescription() != null) {
            recipient.setDescription(donor.getDescription());
        }
        if (donor.getAvailable() != null) {
            recipient.setAvailable(donor.getAvailable());
        }
        return recipient;
    }

    private List<Item> itemsWithStartAndEnd(List<Item> list, long userId) {
        List<Item> resultItems = new ArrayList<>();
        List<Booking> bookings = bookingRepository.findBookingsByItemOwnerId(userId);
        for (Item item : list) {
            Collection<Booking> bookingsOfItem = bookings.stream()
                    .filter(i -> i.getItem().getId() == item.getId())
                    .collect(Collectors.toList());
            item.setNextBooking(getNextBooking(bookingsOfItem));
            item.setLastBooking(getLastBooking(bookingsOfItem));
            resultItems.add(item);
        }
        return resultItems;
    }

    public Booking getNextBooking(Collection<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> allStarts = new ArrayList<>();
        for (Booking booking : bookings) {
            allStarts.add(booking.getStart());
        }
        Optional<LocalDateTime> next = allStarts.stream()
                .filter(date -> date.isAfter(now))
                .min(LocalDateTime::compareTo);

        return bookings.stream().filter(b -> b.getStart() == next.orElse(null)).findFirst().orElse(null);
    }

    public Booking getLastBooking(Collection<Booking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> allEnds = new ArrayList<>();
        for (Booking booking : bookings) {
            allEnds.add(booking.getEnd());
        }
        Optional<LocalDateTime> last = allEnds.stream()
                .filter(date -> date.isBefore(now))
                .max(LocalDateTime::compareTo);

        return bookings.stream().filter(b -> b.getEnd() == last.orElse(null)).findFirst().orElse(null);
    }
}
