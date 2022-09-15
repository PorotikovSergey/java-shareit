package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    Comment commentOne;
    Comment commentTwo;
    Comment commentThree;

    @BeforeEach
    void beforeEach() {
//        commentOne = new Comment(1L, 1L, 2L, "Коммент");
//        commentTwo = new Comment(2L, 2L, 1L, "Комментарий");
//        commentThree = new Comment(3L, 2L, 3L, "Суперкомментарий");

        commentRepository.save(commentOne);
        commentRepository.save(commentTwo);
        commentRepository.save(commentThree);
    }

    @Test
    void findAllByItemId() {
        List<Comment> list = commentRepository.findAllByItemId(2L);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Суперкомментарий", list.get(1).getText());
        assertEquals("Комментарий", list.get(0).getText());
    }

    @AfterEach
    void afterEach() {
        commentRepository.deleteAll();
    }
}