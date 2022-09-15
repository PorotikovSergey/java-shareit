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
    public List<Item> getAll(String owner, String from, String size) {
        long ownerId = Long.parseLong(owner);
        List<Item> allItemsByOwner = itemRepository.findAllByOwnerId(ownerId);
        List<Item> allItems = itemsWithStartAndEnd(allItemsByOwner, ownerId);

        if (from != null) {
            int firstEl = Integer.parseInt(from);
            int sizePage = Integer.parseInt(size);
            return getPageable(allItems, firstEl, sizePage, ownerId);
        }
        return allItems;
    }

    @Override
    public Item postItem(Item item, String ownerId) {
        long idOfOwner = Long.parseLong(ownerId);
        item.setOwner(userRepository.findById(idOfOwner).orElseThrow(() -> new NotFoundException("Такого юзера нет")));
        validateItem(item, ownerId);
        itemRepository.save(item);
        return item;
    }

    @Override
    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public Item patchItem(long itemId, Item item, String ownerId) {
        validateItemForPatch(ownerId, itemId);
        Item forPatch = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        patchOneItemFromAnother(item, forPatch);
        itemRepository.save(forPatch);
        return forPatch;
    }

    @Override
    public Item getItem(String user, long itemId) {
        long userId = Long.parseLong(user);
        List<Booking> bookingsByUser = bookingRepository.findAllByBookerIdOrItemOwnerId(userId, userId);
        Item resultItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        if (Long.parseLong(user) == resultItem.getOwner().getId()) {
            resultItem.setNextBooking(getNextBooking(bookingsByUser));
            resultItem.setLastBooking(getLastBooking(bookingsByUser));
        }
        resultItem.setComments(commentRepository.findAllByItemId(itemId));
        return resultItem;
    }

    @Override
    public List<Item> searchItem(String text, String owner, String from, String size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        List<Item> foundNames = itemRepository
                .findAllByNameContainingIgnoreCaseAndAvailableIs(text, true);
        List<Item> foundDescriptions = itemRepository
                .findAllByDescriptionContainingIgnoreCaseAndAvailableIs(text, true);
        foundNames.removeAll(foundDescriptions);
        foundNames.addAll(foundDescriptions);
        if (from != null) {
            int firstEl = Integer.parseInt(from);
            int sizePage = Integer.parseInt(size);
            long ownerId = Long.parseLong(owner);
            return getPageable(foundNames, firstEl, sizePage, ownerId);
        }
        return foundNames;
    }

    @Override
    public Comment postComment(String bookerId, long itemId, Comment comment) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        long idOfBooker = Long.parseLong(bookerId);

        Booking booking = bookingRepository.findAllByItemId(itemId).stream()
                .filter(b -> b.getBooker().getId() == idOfBooker)
                .findFirst()
                .orElse(null);

        checkCommentBeforePosting(booking, comment);
        comment.setItem(item);
        comment.setAuthorName(userRepository.findById(idOfBooker)
                .orElseThrow(() -> new NotFoundException("Такого юзера нет"))
                .getName());
        commentRepository.save(comment);
        return comment;
    }

    @Override
    public Item postItemToRequest(Item item, String itemOwner, long requestId) {
        validateItem(item, itemOwner);
        User owner = userRepository.findById(Long.parseLong(itemOwner))
                .orElseThrow(() -> new NotFoundException("Такого юзера нет"));
        item.setOwner(owner);
        item.setRequestId(requestId);
        itemRepository.save(item);
        return item;
    }

//-----------------------------private------------------------------------------------------------

    private List<Item> getPageable(List<Item> items, int firstEl, int sizePage, long userId) {
        PagedListHolder<Item> page = new PagedListHolder<>(new ArrayList<>(items.subList(firstEl, items.size())));
        page.setPageSize(sizePage);
        page.setPage(0);
        return itemsWithStartAndEnd(page.getPageList(), userId);
    }

    private void checkCommentBeforePosting(Booking booking, Comment comment) {
        if (booking == null) {
            throw new NotFoundException("Бронирования на данный айтем не было");
        }
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Отзывы возможны только к прошедшим броням");
        }
        if (comment.getText().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым");
        }
    }


    private void validateItem(Item item, String ownerId) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует айди владельца");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Вещь без доступности.");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Вещь с пустым именем.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Вещь с пустым описанием");
        }
    }

    private void validateItemForPatch(String ownerId, long id) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Такого айтема нет"));
        if (Long.parseLong(ownerId) != item.getOwner().getId()) {
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

    private Booking getNextBooking(Collection<Booking> bookings) {
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

    private Booking getLastBooking(Collection<Booking> bookings) {
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