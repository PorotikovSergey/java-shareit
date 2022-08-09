package ru.practicum.shareit.item;

public class ItemIdManager {
    private static long id = 1;

    public static long getItemId() {
        return id++;
    }
}
