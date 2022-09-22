package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.*;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @Min(0)
    private long id;

    private String name;

    @Pattern(regexp = "\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*\\.\\w{2,4}")
    private String email;
}