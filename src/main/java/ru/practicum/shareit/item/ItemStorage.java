package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServiceException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemStorage {
    private final UserStorage userStorage;
    private final List<Item> items = new ArrayList<>();

    public ItemStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<Item> getAll(String ownerId) {
        return items.stream()
                .filter(i->i.getOwnerId()==Long.parseLong(ownerId))
                .collect(Collectors.toList());
    }

    public Item addItem(Item item, String ownerId) {
        validateItem(item, ownerId);
        item.setOwnerId(Long.parseLong(ownerId));
        item.setId(ItemIdManager.getItemId());
        items.add(item);
        return item;
    }

    public Item getItem(long id) {
        return items.stream()
                .filter(i->i.getId()==id)
                .findFirst()
                .orElse(null);
    }

    public void deleteItem(long id) {
        items.remove(getItem(id));
    }

    public Item patchItem(long id, Item newItem, String ownerId) {
        validateItemForPatch(ownerId, id);
        Item item = patchOneItemFromAnother(newItem, getItem(id));
        deleteItem(id);
        items.add(item);
        return item;
    }

    public Collection<Item> searchItem(String text) {
        return items.stream()
                .filter(i->checkTextInDescriptionAndName(i, text) && i.getAvailable())
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

    private void validateItem(Item item, String ownerId) {
        if (ownerId==null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (userStorage.getUser(Long.parseLong(ownerId))==null) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
        if (item.getAvailable()==null) {
            throw new ValidationException("Вещь без доступности.");
        }
        if (item.getName()==null || item.getName().isBlank()) {
            throw new ValidationException("Вещь с пустым именем.");
        }
        if (item.getDescription()==null || item.getDescription().isBlank()) {
            throw new ValidationException("Вещь с пустым описанием");
        }
    }

    private void validateItemForPatch(String ownerId, long id) {
        if (ownerId==null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (userStorage.getUser(Long.parseLong(ownerId)) == null) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
        if (Long.parseLong(ownerId)!=getItem(id).getOwnerId()) {
            throw new NotFoundException("Патчить вещь может только её владелец.");
        }
    }

    private Item patchOneItemFromAnother(Item donor, Item recipient) {
        if (donor.getName()!=null) {
            recipient.setName(donor.getName());
        }
        if (donor.getDescription()!=null) {
            recipient.setDescription(donor.getDescription());
        }
        if (donor.getAvailable()!=null) {
            recipient.setAvailable(donor.getAvailable());
        }
        return recipient;
    }
}