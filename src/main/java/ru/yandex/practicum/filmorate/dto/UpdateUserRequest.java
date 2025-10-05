package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private Long id;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;
    @NotBlank(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;
    private String name;
    @Past(message = "Некорректная дата рождения")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    public boolean hasLogin() {
        return ! (login == null || login .isBlank());
    }

    public boolean hasEmail() {
        return ! (email == null || email .isBlank());
    }

    public boolean hasName() {
        return ! (name == null || name .isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null);
    }
}
