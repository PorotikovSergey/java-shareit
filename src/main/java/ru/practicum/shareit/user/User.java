package ru.practicum.shareit.user;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "requests")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;
}