package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ServiceException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public Collection<Item> getAll(String ownerId) {
        return itemRepository.findAll();
    }

    public Item postItem(Item item, String ownerId) {
        item.setOwner(Long.parseLong(ownerId));
        itemRepository.save(item);
        return item;
    }

    public void deleteItem(long itemId) {
        itemRepository.deleteById(itemId);
    }

    public Item patchItem(long itemId, Item item, String ownerId) {
        validateItemForPatch(ownerId, itemId);
        validateItem(item, ownerId);
        return patchOneItemFromAnother(item, getItem(itemId));
    }

    public Item getItem(long itemId) {
        return itemRepository.getReferenceById(itemId);
    }

    public Collection<Item> searchItem(String text) {
        return null;
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
        if (Long.parseLong(ownerId) != getItem(id).getOwner()) {
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
}