package ru.practicum.shareit.mapper;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.mapper.request.Request;
import ru.practicum.shareit.mapper.request.RequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;

import java.util.ArrayList;
import java.util.List;

@Service
public class Mapper {

    public UserDto fromUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public User fromDtoToUser(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }

    public BookingDto fromBookingToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBooker(fromUserToDto(booking.getBooker()));
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setItem(fromItemToDtoInner(booking.getItem()));
        return bookingDto;
    }

    public Booking fromDtoToBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        return booking;
    }

    public RequestDto fromRequestToDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setDescription(request.getDescription());

        for (Item item : request.getItems()) {
            requestDto.getItems().add(fromItemToDtoInner(item));
        }

        return requestDto;
    }

    public Request fromDtoToRequest(RequestDto requestDto) {
        Request request = new Request();
        request.setId(requestDto.getId());
        request.setCreated(requestDto.getCreated());
        request.setDescription(requestDto.getDescription());
        return request;
    }

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

    public ItemDto fromItemToDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setLastBooking(fromBookingToDtoInItem(item.getLastBooking()));
        itemDto.setNextBooking(fromBookingToDtoInItem(item.getNextBooking()));
        List<Comment> commentsBefore = item.getComments();
        if (commentsBefore != null) {
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

    public Item fromPatchDtoToItem(ItemPatchDto itemPatchDto) {
        Item item = new Item();
        item.setName(itemPatchDto.getName());
        item.setDescription(itemPatchDto.getDescription());
        item.setAvailable(itemPatchDto.getAvailable());
        return item;
    }

    private BookingDto fromBookingToDtoInItem(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setBooker(fromUserToDto(booking.getBooker()));
        bookingDto.setBookerId(booking.getBooker().getId());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setItem(fromItemToDtoInner(booking.getItem()));
        return bookingDto;
    }

    private ItemDto fromItemToDtoInner(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwner().getId());
        itemDto.setRequestId(item.getRequestId());
        return itemDto;
    }
}