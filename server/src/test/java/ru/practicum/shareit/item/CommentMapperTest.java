package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.mapper.Mapper;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {
    Mapper mapper = new Mapper();

    CommentDto commentDto = new CommentDto(1L, "comment-1", "Bob Brown");
    Item item = new Item(3L, "item-1", "description-1", true,
            17, new User(), new ArrayList<>(), null, null);
    Comment comment = new Comment(23L, item, "Bob", "text of comment");

    @Test
    void fromCommentToDto() {
        CommentDto newDto = mapper.fromCommentToDto(comment);

        assertNotNull(newDto);
        assertEquals(23L, newDto.getId());
        assertEquals("Bob", newDto.getAuthorName());
        assertEquals("text of comment", newDto.getText());
    }

    @Test
    void fromDtoToComment() {
        Comment newComment = mapper.fromDtoToComment(commentDto);

        assertNotNull(newComment);
        assertEquals(1L, newComment.getId());
        assertEquals("Bob Brown", newComment.getAuthorName());
        assertEquals("comment-1", newComment.getText());
    }
}
