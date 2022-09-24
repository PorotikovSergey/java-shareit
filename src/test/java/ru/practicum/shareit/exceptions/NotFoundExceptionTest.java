package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {
    @Test
    void checkError() {
        Throwable thrown = assertThrows(Exception.class, () -> {
            throw new NotFoundException("ошибка");
        });
        assertNotNull(thrown.getMessage());
    }

}