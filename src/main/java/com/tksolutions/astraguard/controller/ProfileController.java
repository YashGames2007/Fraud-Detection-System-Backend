package com.tksolutions.astraguard.controller;

import com.tksolutions.astraguard.dto.UserProfileResponse;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {

        AuthUser authUser = (AuthUser)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        UserProfileResponse response = userService.getProfile(authUser);

        return ResponseEntity.ok(response);
    }
}
