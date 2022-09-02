package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Collection<Item> getAll(String ownerId) {
        List<Item> allItemsByOwner = itemRepository.findAllByOwnerId(Long.parseLong(ownerId));
        return itemsWithStartAndEnd(allItemsByOwner, ownerId);
    }

    public Item postItem(Item item, String ownerId) {
        item.setOwnerId(Long.parseLong(ownerId));
        validateItem(item, ownerId);
        itemRepository.save(item);
        return item;
    }

    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    public Item patchItem(long itemId, Item item, String ownerId) {
        validateItemForPatch(ownerId, itemId);
        itemRepository.save(patchOneItemFromAnother(item, getItemWithOutUser(itemId)));
        return getItemWithOutUser(itemId);
    }

    public Item getItemWithOutUser(long itemId) {
        checkItem(itemId);
        return itemRepository.getReferenceById(itemId);
    }

    public Item getItem(String user, long itemId) {
        long userId = Long.parseLong(user);
        checkItem(itemId);
        Collection<Booking> bookingsByUser = bookingRepository.findAllByBookerIdOrItemOwnerId(userId, userId);
        Item resultItem = itemRepository.getReferenceById(itemId);
        if(Long.parseLong(user)== resultItem.getOwnerId()) {
            resultItem.setNextBooking(getNextBooking(bookingsByUser));
            resultItem.setLastBooking(getLastBooking(bookingsByUser));
        }
        List<Comment> commentsForItem = commentRepository.findAllByItemId(itemId);
        for(Comment comment: commentsForItem) {
            comment.setAuthorName(userRepository.getReferenceById(comment.getBookerId()).getName());
        }
        resultItem.setComments(commentsForItem);
        return resultItem;
    }

    public Collection<Item> searchItem(String text, String ownerId) {
        if(text.isBlank()) {
            return new ArrayList<>();
        }
        Collection<Item> foundNames = itemRepository
                .findAllByNameContainingIgnoreCaseAndAvailableIs(text, true);
        Collection<Item> foundDescriptions = itemRepository
                .findAllByDescriptionContainingIgnoreCaseAndAvailableIs(text, true);
        foundNames.removeAll(foundDescriptions);
        foundNames.addAll(foundDescriptions);
        return foundNames;
    }

    @Override
    public Comment postComment(String bookerId, long itemId, Comment comment) {
        Item item = getItemWithOutUser(itemId);
        long idOfBooker = Long.parseLong(bookerId);
        Collection<Booking> col = bookingRepository.findAllByItemId(itemId);
        Booking booking = col.stream()
                .filter(b -> b.getBookerId()==idOfBooker)
                .findFirst()
                .orElse(null);

        if(booking==null) {
            throw new NotFoundException("Бронирования на данный айтем не было");
        }
        if(booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Отзывы возможны только к прошедшим броням");
        }
        if(comment.getText().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым");
        }
        comment.setText(comment.getText());
        comment.setItemId(itemId);
        comment.setBookerId(idOfBooker);
        comment.setAuthorName(userRepository.getReferenceById(idOfBooker).getName());
        commentRepository.save(comment);
        item.getComments().add(comment);
        return comment;
    }

//------------------------------------------------------------------------------------------
    private void validateItem(Item item, String ownerId) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (!userRepository.existsById(Long.parseLong(ownerId))) {
            throw new NotFoundException("С таким Id владельца не существует");
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
            throw new NotFoundException("Айтема с таким id не существует");
        }
    }

    private void validateItemForPatch(String ownerId, long id) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (!userRepository.existsById(Long.parseLong(ownerId))) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
        if (Long.parseLong(ownerId) != getItemWithOutUser(id).getOwnerId()) {
            throw new NotFoundException("Патчить вещь может только её владелец.");
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

    private List<Item> itemsWithStartAndEnd (List<Item> list, String ownerId) {
        List<Item> resultItems = new ArrayList<>();
        Collection<Booking> bookings = bookingRepository.findBookingsByItemOwnerId(Long.parseLong(ownerId));
        for(Item item: list) {
            Collection<Booking> bookingsOfItem = bookings.stream()
                    .filter(i -> i.getItemId()== item.getId())
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
        for(Booking booking: bookings) {
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
        for(Booking booking: bookings) {
            allEnds.add(booking.getEnd());
        }
        Optional<LocalDateTime> last = allEnds.stream()
                .filter(date -> date.isBefore(now))
                .max(LocalDateTime::compareTo);

        return bookings.stream().filter(b -> b.getEnd()== last.orElse(null)).findFirst().orElse(null);
    }
}