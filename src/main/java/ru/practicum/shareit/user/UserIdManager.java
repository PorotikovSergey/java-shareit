package ru.practicum.shareit.user;

public class UserIdManager {
    private static long id = 1;

    public static long getUserId() {
        return id++;
    }
}
