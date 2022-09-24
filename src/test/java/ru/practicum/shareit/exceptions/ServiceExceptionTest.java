package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceExceptionTest {
    @Test
    void checkError() {
        Throwable thrown = assertThrows(Exception.class, () -> {
            throw new ServiceException("ошибка");
        });
        assertNotNull(thrown.getMessage());
    }
}