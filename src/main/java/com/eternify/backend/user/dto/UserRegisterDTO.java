package com.eternify.backend.user.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String password;
    private String email;
}
