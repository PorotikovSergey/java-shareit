package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServiceException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.*;

@Slf4j
@Repository
@Qualifier("itemStorage")
public class ItemStorage {
    private final UserStorage userStorage;
    private Map<Long, Item> items = new HashMap();

    public ItemStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<Item> getAll() {
        return items.values();
    }

    public Item addItem(Item item, String ownerId) {
        validateItem(item, ownerId);
        item.setOwnerId(Long.parseLong(ownerId));
        item.setId(ItemIdManager.getItemId());
        items.put(item.getId(), item);
        return item;
    }

    public Item getItem(long id) {
        return items.get(id);
    }

    public void deleteItem(long id) {
        items.remove(id);
    }

    public Item patchItem(long id, Item newItem, String ownerId) {
        validateItemForPatch(newItem, ownerId);
        if(Long.parseLong(ownerId)!= items.get(id).getOwnerId()) {
            throw new NotFoundException("Патчить вещь может только её владелец.");
        }
        Item item = patchOneItemFromAnother(newItem, items.get(id));
        items.replace(id, item);
        return item;
    }

    public Collection<Item> searchItem(String text) {
        Collection<Item> resultList = new ArrayList<>();
        Collection<Item> itemList = items.values();
        for (Item item: itemList) {
            if(checkTextInDescriptionAndName(item, text)) {
                resultList.add(item);
            }
        }
        return resultList;
    }

//==================================================================================================

    private boolean checkTextInDescriptionAndName(Item item, String text) {
        String checkText = text.toLowerCase();
        String description = item.getDescription().toLowerCase();
        String name = item.getName().toLowerCase();
        return description.contains(checkText) || name.contains(checkText);
    }

    private void validateItem(Item item, String ownerId) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (userStorage.getUser(Long.parseLong(ownerId)) == null) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
        if (!item.isAvailable()) {
            throw new ru.practicum.shareit.exceptions.ValidationException("Вещь с доступностью false.");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ru.practicum.shareit.exceptions.ValidationException("Вещь с пустым именем.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Вещь с пустым описанием");
        }
     }

    private void validateItemForPatch(Item item, String ownerId) {
        if (ownerId == null) {
            throw new ServiceException("Отсутствует владелец");
        }
        if (userStorage.getUser(Long.parseLong(ownerId)) == null) {
            throw new NotFoundException("С таким Id владельца не существует");
        }
    }

    private Item patchOneItemFromAnother(Item donor, Item recipient) {
        if(donor.getName()!=null) {
            recipient.setName(donor.getName());
        }
        if(donor.getDescription()!=null) {
            recipient.setDescription(donor.getDescription());
        }
        recipient.setAvailable(donor.isAvailable());
        return recipient;
    }
}
