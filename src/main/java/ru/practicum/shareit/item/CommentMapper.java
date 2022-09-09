package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;

@Service
public class CommentMapper {
    public CommentDto fromCommentToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthorName());
        return commentDto;
    }

    public Comment fromDtoToComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setText(commentDto.getText());
        comment.setAuthorName(commentDto.getAuthorName());
        return comment;
    }
}
