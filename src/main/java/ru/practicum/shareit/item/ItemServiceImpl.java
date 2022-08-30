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

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    //вот этот метод переделать на нормальный, что б в запросе, а не через стрим!!!
    public Collection<Item> getAll(String ownerId) {
        Collection<Item> resultCollection = new ArrayList<>();
        Collection<Item> col = itemRepository.findAll().stream()
                .filter(i -> i.getOwnerId() == Long.parseLong(ownerId))
                .collect(Collectors.toList());
        Collection<Booking> col2 = bookingRepository.findAll().stream().filter(i -> i.getItemOwnerId()==Long.parseLong(ownerId)).collect(Collectors.toList());
        System.out.println("ВСЕ БРОНИ - "+bookingRepository.findAll());
        for(Item item: col) {
            Collection<Booking> col3 = col2.stream().filter(i -> i.getItemId()== item.getId()).collect(Collectors.toList());
            item.setNextBooking(getNextBooking(col3));
            item.setLastBooking(getLastBooking(col3));
        }
        resultCollection = col;
        return resultCollection;
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
        itemRepository.save(patchOneItemFromAnother(item, getItem1(itemId)));
        return getItem1(itemId);
    }

    //-----тут нужно убрать вариант без юзера, так как он всегда передаётся--------
    public Item getItem(String user, long itemId) {
            return getItem2(user, itemId);
    }

    public Item getItem1(long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Айтема с таким id не существует");
        }
        return itemRepository.getReferenceById(itemId);
    }

    public Item getItem2(String user, long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Айтема с таким id не существует");
        }
        Collection<Booking> col = bookingRepository.findAll().stream().filter(i -> i.getItemId()==itemId).filter(i -> i.getItemOwnerId()==Long.parseLong(user)).collect(Collectors.toList());
        Item resultItem = itemRepository.getReferenceById(itemId);
        if(Long.parseLong(user)== resultItem.getOwnerId()) {
            resultItem.setNextBooking(getNextBooking(col));
            resultItem.setLastBooking(getLastBooking(col));
        }
        return resultItem;
    }

    //Этот ебанутый метод тоже нужно из стрима переделать в нормальный запрос
    public Collection<Item> searchItem(String text, String ownerId) {
        if(text.isBlank()) {
            return new ArrayList<Item>();
        }
        return itemRepository.findAll().stream()
                .filter(i -> i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    private boolean checkTextInDescriptionAndName(Item item, String text) {
        if (text.isBlank()) {
            return false;
        }
        String checkText = text.toLowerCase();
        String description = item.getDescription().toLowerCase();
        String name = item.getName().toLowerCase();
        return description.contains(checkText) || name.contains(checkText);
    }

    private Item setInnerBookingsForItem(Item item) {
        Item resultItem = new Item();
        resultItem.setId(item.getId());
        resultItem.setName(item.getName());
        resultItem.setDescription(item.getDescription());
        resultItem.setOwnerId(item.getOwnerId());
        resultItem.setAvailable(item.getAvailable());
        return resultItem;
    }

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

    private void validateItemForPatch(String ownerId, long id) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (!userRepository.existsById(Long.parseLong(ownerId))) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
        if (Long.parseLong(ownerId) != getItem1(id).getOwnerId()) {
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

    private Booking getNextBooking(Collection<Booking> bookings) {
        Booking bookingg = new Booking();
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> allStarts = new ArrayList<>();
        for(Booking booking: bookings) {
            allStarts.add(booking.getStart());
        }
        Optional<LocalDateTime> next = allStarts.stream()
                .filter(date -> date.isAfter(now))
                .min(LocalDateTime::compareTo);

        Optional<Booking> nextBooking = bookings.stream().filter(b -> b.getStart()== next.orElse(null)).findFirst();
        bookingg =nextBooking.orElse(null);
        return bookingg;
    }

    private Booking getLastBooking(Collection<Booking> bookings) {
        Booking bookingg = new Booking();
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> allEnds = new ArrayList<>();
        for(Booking booking: bookings) {
            allEnds.add(booking.getEnd());
        }
        Optional<LocalDateTime> last = allEnds.stream()
                .filter(date -> date.isBefore(now))
                .max(LocalDateTime::compareTo);

        Optional<Booking> lastBooking = bookings.stream().filter(b -> b.getEnd()== last.orElse(null)).findFirst();

        bookingg =lastBooking.orElse(null);
        return bookingg;
    }
}