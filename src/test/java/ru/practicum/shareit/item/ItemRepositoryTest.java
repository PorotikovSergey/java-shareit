//package ru.practicum.shareit.item;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import ru.practicum.shareit.requests.user.User;
//import ru.practicum.shareit.requests.user.UserRepository;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//class ItemRepositoryTest {
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    ItemRepository itemRepository;
//
//    User userOne;
//    User userTwo;
//    User userThree;
//    User userFour;
//
//    Item itemOne;
//    Item itemTwo;
//    Item itemThree;
//    Item itemFour;
//
//    @BeforeEach
//    void beforeEach() {
//        userOne = new User(1L, "Bob", "bob@mail.ru");
//        userRepository.save(userOne);
//
//        itemOne = new Item(1L, "item-1", "description-1", true, 1L, 100L,
//                null, null, null);
//        itemRepository.save(itemOne);
//
//        userTwo = new User(2L, "Mary", "mary@ya.ru");
//        userRepository.save(userTwo);
//        itemTwo = new Item(2L, "item-2", "description-2", true, 2L, 99L,
//                null, null, null);
//        itemRepository.save(itemTwo);
//
//        userThree = new User(3L, "John", "john@gmail.com");
//        userRepository.save(userThree);
//        itemThree = new Item(3L, "item-3", "description-3", true, 3L, 98L,
//                null, null, null);
//        itemRepository.save(itemThree);
//
//        userFour = new User(4L, "Lida", "lida@yandex.ru");
//        userRepository.save(userFour);
//        itemFour = new Item(4L, "item-4", "description-3", false, 4L, 97L,
//                null, null, null);
//        itemRepository.save(itemFour);
//    }
//
//    @Test
//    void findAllByNameContainingIgnoreCaseAndAvailableIs() {
//        assertEquals(1, 1);
//        List<Item> list = itemRepository.findAllByNameContainingIgnoreCaseAndAvailableIs("-2", true);
//        assertNotNull(list);
//        assertEquals(1, list.size());
//        assertEquals("item-2", list.get(0).getName());
//
//        List<Item> list2 = itemRepository.findAllByNameContainingIgnoreCaseAndAvailableIs("-4", true);
//        assertEquals(0, list2.size());
//
//        itemRepository.deleteAll();
//        userRepository.deleteAll();
//    }
//
//    @Test
//    void findAllByOwnerId() {
//
//        assertEquals(1, 1);
//        List<Item> list = itemRepository.findAllByOwnerId(1L);
//        assertNotNull(list);
//        assertEquals(1, list.size());
//        assertEquals("item-1", list.get(0).getName());
//    }
//
//    @Test
//    void findAllByOwnerId2() {
//        List<Item> list1 = itemRepository.findAllByOwnerId(1L);
//        assertNotNull(list1);
//        assertEquals(1, list1.size());
//        assertEquals("item-1", list1.get(0).getName());
//
//    }
//
//    @Test
//    void findAllByDescriptionContainingIgnoreCaseAndAvailableIs() {
//        List<Item> list = itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailableIs("ion",
//                true);
//        assertNotNull(list);
//        assertEquals(3, list.size());
//
//        List<Item> list2 = itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailableIs("ion",
//                false);
//        assertEquals(1, list2.size());
//    }
//
//    @Test
//    void findAllByRequestId() {
//        List<Item> list = itemRepository.findAllByRequestId(99);
//        assertNotNull(list);
//        assertEquals(1, list.size());
//        assertEquals("item-2", list.get(0).getName());
//    }
//
//    @AfterEach
//    void afterEach() {
//        itemRepository.deleteAll();
//        userRepository.deleteAll();
//    }
//}