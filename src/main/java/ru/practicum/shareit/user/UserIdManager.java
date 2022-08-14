package ru.practicum.shareit.user;

public class UserIdManager {
    private static long id = 1;

    private UserIdManager() {
    }

    public static long getUserId() {
        return id++;
    }
}