package com.tksolutions.astraguard.service;

import com.tksolutions.astraguard.dto.UserProfileResponse;
import com.tksolutions.astraguard.exception.UserNotFoundException;
import com.tksolutions.astraguard.model.AuthUser;
import com.tksolutions.astraguard.model.entity.UserEntity;
import com.tksolutions.astraguard.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getProfile(AuthUser authUser) {

        UserEntity user = userRepository
                .findById(authUser.getUserId())
                .orElseThrow(UserNotFoundException::new);

        return new UserProfileResponse(
                user.getUpiId(),
                user.getName(),
                user.getMobile()
        );
    }
}
