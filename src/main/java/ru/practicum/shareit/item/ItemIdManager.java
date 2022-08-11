package ru.practicum.shareit.item;

public class ItemIdManager {
    private static long id = 1;

    private ItemIdManager() {
    }

    public static long getItemId() {
        return id++;
    }
}
