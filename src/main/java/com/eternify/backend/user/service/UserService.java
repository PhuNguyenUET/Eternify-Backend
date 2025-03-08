package com.eternify.backend.user.service;



import com.eternify.backend.user.dto.ChangePasswordRequest;
import com.eternify.backend.user.dto.CreateNewPasswordRequest;
import com.eternify.backend.user.dto.UserEditDTO;
import com.eternify.backend.user.dto.UserRegisterDTO;
import com.eternify.backend.user.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.text.ParseException;
import java.util.List;

public interface UserService extends UserDetailsService {
    void changePassword(ChangePasswordRequest changePasswordRequest);

    void sendResetPasswordEmail(String email);

    void createNewPassword(CreateNewPasswordRequest request);

    void changeEmail(String newEmail);

    void register (UserRegisterDTO dto);

    void sendConfirmEmail();

    void confirmEmail(String token);

    User getUserByUsername(String username);

    List<User> findAll();

    User getCurrentUser();

    void editUser(UserEditDTO dto) throws ParseException;
}
