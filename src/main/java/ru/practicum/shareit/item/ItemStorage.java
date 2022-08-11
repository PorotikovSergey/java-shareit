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
    private List<Item> items = new ArrayList<>();

    public ItemStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<Item> getAll(String ownerId) {
        Collection<Item> resultCollection = new ArrayList<>();
        for(Item item: items) {
            if(item.getOwnerId()==Long.parseLong(ownerId)) {
                resultCollection.add(item);
            }
        }
        return resultCollection;
    }

    public Item addItem(Item item, String ownerId) {
        validateItem(item, ownerId);
        item.setOwnerId(Long.parseLong(ownerId));
        item.setId(ItemIdManager.getItemId());
        items.add(item);
        return item;
    }

    public Item getItem(long id) {
        for(Item item: items) {
            if(item.getId()==id) {
                return item;
            }
        }
        return null;
    }

    public void deleteItem(long id) {
        items.remove(getItem(id));
    }

    public Item patchItem(long id, Item newItem, String ownerId) {
        validateItemOwnerForPatch(ownerId);
        if(Long.parseLong(ownerId)!= getItem(id).getOwnerId()) {
            throw new NotFoundException("Патчить вещь может только её владелец.");
        }
        Item item = patchOneItemFromAnother(newItem, getItem(id));
        deleteItem(id);
        items.add(item);
        return item;
    }

    public Collection<Item> searchItem(String text) {
        Collection<Item> resultList = new ArrayList<>();
        if(!text.isBlank()) {
            for (Item item : items) {
                if (checkTextInDescriptionAndName(item, text) && item.getAvailable()) {
                    resultList.add(item);
                }
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
        if (item.getAvailable()==null) {
            throw new ValidationException("Вещь без доступности.");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Вещь с пустым именем.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Вещь с пустым описанием");
        }
     }

    private void validateItemOwnerForPatch(String ownerId) {
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
        if(donor.getAvailable()!=null) {
            recipient.setAvailable(donor.getAvailable());
        }
        return recipient;
    }
}
