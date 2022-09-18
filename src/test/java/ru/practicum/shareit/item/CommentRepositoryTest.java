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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    User userOne;
    User userTwo;
    User userThree;

    Item itemOne;
    Item itemTwo;
    Item itemThree;

    Comment commentOne;
    Comment commentTwo;
    Comment commentThree;

    @BeforeEach
    void beforeEach() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

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
        itemRepository.save(itemThree);

        commentOne = new Comment(1L, itemOne, userOne.getName(), "Коммент");
        commentTwo = new Comment(2L, itemTwo, userTwo.getName(), "Комментарий");
        commentThree = new Comment(3L, itemThree, userThree.getName(), "Суперкомментарий");

        commentRepository.save(commentOne);
        commentRepository.save(commentTwo);
        commentRepository.save(commentThree);
    }

    @Test
    void findAllByItemId() {
        List<Comment> list = commentRepository.findAllByItemId(2L);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Комментарий", list.get(0).getText());
    }

    @AfterEach
    void afterEach() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}