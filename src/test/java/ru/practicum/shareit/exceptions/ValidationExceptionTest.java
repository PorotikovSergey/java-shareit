package ru.practicum.shareit.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
    @Test
    void checkError() {
        Throwable thrown = assertThrows(Exception.class, () -> {
            throw new ValidationException("ошибка");
        });
        assertNotNull(thrown.getMessage());
    }
}