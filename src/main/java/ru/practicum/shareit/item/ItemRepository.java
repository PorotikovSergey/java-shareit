package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(long ownerId);

    List<Item> findAllByNameContainingIgnoreCaseAndAvailableIs(String search, Boolean available);

    List<Item> findAllByDescriptionContainingIgnoreCaseAndAvailableIs(String search, Boolean available);
}
