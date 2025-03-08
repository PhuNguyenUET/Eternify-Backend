package com.eternify.backend.util;


import com.eternify.backend.user.model.CustomUserDetails;
import com.eternify.backend.user.model.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class AuthenticationUtils {
    public static User getCurrentUser() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customUserDetails.getUser();
    }
}

