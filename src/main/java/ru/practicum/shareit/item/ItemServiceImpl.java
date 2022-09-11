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
        checkUserId(idOfOwner);

        item.setOwnerId(idOfOwner);
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
        itemRepository.save(patchOneItemFromAnother(item, getItemWithOutUser(itemId)));
        return getItemWithOutUser(itemId);
    }

    @Override
    public Item getItem(String user, long itemId) {
        long userId = Long.parseLong(user);
        checkItem(itemId);
        Collection<Booking> bookingsByUser = bookingRepository.findAllByBookerIdOrItemOwnerId(userId, userId);
        Item resultItem = itemRepository.findById(itemId).get();
        if (Long.parseLong(user) == resultItem.getOwnerId()) {
            resultItem.setNextBooking(getNextBooking(bookingsByUser));
            resultItem.setLastBooking(getLastBooking(bookingsByUser));
        }
        List<Comment> commentsForItem = commentRepository.findAllByItemId(itemId);
        for (Comment comment : commentsForItem) {
            comment.setAuthorName(userRepository.findById(comment.getBookerId()).get().getName());
        }
        resultItem.setComments(commentsForItem);
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
        Item item = getItemWithOutUser(itemId);
        long idOfBooker = Long.parseLong(bookerId);

        Booking booking = bookingRepository.findAllByItemId(itemId).stream()
                .filter(b -> b.getBookerId() == idOfBooker)
                .findFirst()
                .orElse(null);

        checkCommentBeforePosting(booking, comment);

        comment.setText(comment.getText());
        comment.setItemId(itemId);
        comment.setBookerId(idOfBooker);
        comment.setAuthorName(userRepository.findById(idOfBooker).get().getName());
        commentRepository.save(comment);
        item.getComments().add(comment);
        return comment;
    }

    @Override
    public Item postItemToRequest(Item item, String itemOwner, long requestId) {
        item.setOwnerId(Long.parseLong(itemOwner));
        validateItem(item, itemOwner);
        itemRepository.save(item);
        return item;
    }

//-----------------------------private------------------------------------------------------------

    private void checkUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Юзера с таким айди " + userId + " нет");
        }
    }

    private List<Item> getPageable(List<Item> items, int firstEl, int sizePage, long userId) {
        PagedListHolder page = new PagedListHolder(new ArrayList<>(items.subList(firstEl, items.size())));
        page.setPageSize(sizePage);
        page.setPage(0);
        return itemsWithStartAndEnd(page.getPageList(), userId);
    }

    private Item getItemWithOutUser(long itemId) {
        checkItem(itemId);
        return itemRepository.findById(itemId).get();
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
            throw new ServiceException("Отсутствует владелец");
        }
        if (!userRepository.existsById(Long.parseLong(ownerId))) {
            throw new NotFoundException("С таким Id " + ownerId + " владельца не существует");
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

    private void checkItem(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Айтема с таким id " + itemId + " не существует");
        }
    }

    private void validateItemForPatch(String ownerId, long id) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (!userRepository.existsById(Long.parseLong(ownerId))) {
            throw new NotFoundException("С таким Id " + ownerId + " владельца не существует");
        }
        if (Long.parseLong(ownerId) != getItemWithOutUser(id).getOwnerId()) {
            throw new NotFoundException("Патчить вещь может только её владелец с айди "
                    + getItemWithOutUser(id).getOwnerId());
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
        Collection<Booking> bookings = bookingRepository.findBookingsByItemOwnerId(userId);
        for (Item item : list) {
            Collection<Booking> bookingsOfItem = bookings.stream()
                    .filter(i -> i.getItemId() == item.getId())
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