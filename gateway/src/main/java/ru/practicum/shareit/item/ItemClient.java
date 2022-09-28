package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public Collection<ResponseEntity<Object>> getAll(long userId, Integer from, Integer size) {
        return null;
    }

    public ResponseEntity<Object> postItem(ItemDto itemDto, long userId) {
        return null;
    }

    public ResponseEntity<Object> postItemToRequest(ItemDto itemDto, long userId, long requestId) {
        return null;
    }

    public void deleteItem(long itemId) {
        System.out.println("Удаление айтема");
    }

    public ResponseEntity<Object> patchItem(long itemId, ItemDto itemDto, long userId) {
        return null;
    }

    public ResponseEntity<Object> getItem(long userId, long itemId) {
        return null;
    }

    public Collection<ResponseEntity<Object>> searchItem(String text, long userId, Integer from, Integer size) {
        return null;
    }

    public ResponseEntity<Object> postComment(long userId, long itemId, CommentDto commentDto) {
        return null;
    }
}
