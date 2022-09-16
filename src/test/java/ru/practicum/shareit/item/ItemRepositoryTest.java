package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    User userOne;
    User userTwo;
    User userThree;
    User userFour;

    Item itemOne;
    Item itemTwo;
    Item itemThree;
    Item itemFour;

    @BeforeEach
    void beforeEach() {
        userOne = new User(1L, "Bob", "bob@mail.ru");
        userRepository.save(userOne);

        itemOne = new Item(1L, "item-1", "description-1", true, 1L, userOne,
                null, null, null);
        itemRepository.save(itemOne);

        userTwo = new User(2L, "Mary", "mary@ya.ru");
        userRepository.save(userTwo);
        itemTwo = new Item(2L, "item-2", "description-2", true, 2L, userTwo,
                null, null, null);
        itemRepository.save(itemTwo);

        userThree = new User(3L, "John", "john@gmail.com");
        userRepository.save(userThree);
        itemThree = new Item(3L, "item-3", "description-3", true, 3L, userThree,
                null, null, null);
        itemThree.setAvailable(true);
        itemThree.setRequestId(1);
        itemRepository.save(itemThree);

        userFour = new User(4L, "Lida", "lida@yandex.ru");
        userRepository.save(userFour);
        itemFour = new Item(4L, "item-4", "description-4", false, 4L, userFour,
                null, null, null);
        itemRepository.save(itemFour);
    }

    @Test
    void findAllByNameContainingIgnoreCaseAndAvailableIs() {
        List<Item> list = itemRepository.findAllByNameContainingIgnoreCaseAndAvailableIs("-2", true);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("item-2", list.get(0).getName());

        List<Item> list2 = itemRepository.findAllByNameContainingIgnoreCaseAndAvailableIs("3", true);
        assertEquals(1, list2.size());
        assertEquals("item-3", list2.get(0).getName());
        assertTrue(list.get(0).getAvailable());
    }

    @Test
    void findAllByOwnerId() {
        List<Item> list = itemRepository.findAllByOwnerId(1L);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("item-1", list.get(0).getName());
        assertEquals("description-1", list.get(0).getDescription());
    }

    @Test
    void findAllByDescriptionContainingIgnoreCaseAndAvailableIs() {
        List<Item> list = itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailableIs("ion",
                true);
        assertNotNull(list);
        assertEquals(3, list.size());

        List<Item> list2 = itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailableIs("ion",
                false);
        assertEquals(1, list2.size());
    }

    @Test
    void findAllByRequestIdNotExist() {
        List<Item> list = itemRepository.findAllByRequestId(99);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}