package com.eternify.backend.user.model;


import com.eternify.backend.song.model.UserPref;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@Document("z_user")
public class User {
    @Id
    private String id;

    @Indexed(background = true)
    private String email;

    private String password;
    private String phone;
    private String role;
    private boolean active;
    private String firstName;
    private String lastName;
    private String persistentAvatarId;
    private String userDescription;
    private String address;

    private UserPref userPref;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateOfBirth;

    private int failedAttempt;

    private String resetPasswordToken;
    private long resetPasswordTokenExpire;

    private boolean isEmailValidated;
    private String confirmEmailToken;
    private long confirmEmailTokenExpire;
}
