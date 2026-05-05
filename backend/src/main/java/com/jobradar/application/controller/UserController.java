package com.jobradar.application.controller;



import com.jobradar.application.dto.UpdateUserProfileRequest;
import com.jobradar.application.dto.UserProfileResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController (UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(Authentication auth){
        String email = auth.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User isn't found"));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEmailVerified(),
                false,
                user.getCreatedAt()
        );
    }

    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUser(Authentication auth,
                                                 @RequestBody UpdateUserProfileRequest request){
        String email = auth.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User don't found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name().trim());
        }

        User savedUser = userRepository.save(user);

        return new UserProfileResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.isEmailVerified(),
                false,
                savedUser.getCreatedAt()
        );


    }

}
