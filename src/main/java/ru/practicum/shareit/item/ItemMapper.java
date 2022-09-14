package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemMapper {

    public ItemDto fromItemToDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setLastBooking(fromBookingToDto(item.getLastBooking()));
        itemDto.setNextBooking(fromBookingToDto(item.getNextBooking()));
        List<Comment> commentsBefore = item.getComments();
        if(commentsBefore!=null) {
            List<CommentDto> commentsAfter = new ArrayList<>();
            for (Comment comment : item.getComments()) {
                commentsAfter.add(fromCommentToDto(comment));
            }
            itemDto.setComments(commentsAfter);
        }
        itemDto.setRequestId(item.getRequestId());
        return itemDto;
    }


    public Item fromDtoToItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }
    private BookingDto fromBookingToDto(Booking booking) {
        if(booking==null) {
            return null;
        }
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBooker(booking.getBooker());
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setState(booking.getState());
        bookingDto.setItem(booking.getItem());
        return bookingDto;
    }

    public CommentDto fromCommentToDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthorName());
        return commentDto;
    }
}