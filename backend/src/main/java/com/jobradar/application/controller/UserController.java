package com.jobradar.application.controller;



import com.jobradar.application.dto.UserProfileResponse;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
