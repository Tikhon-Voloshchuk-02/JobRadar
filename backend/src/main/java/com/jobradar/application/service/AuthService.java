package com.jobradar.application.service;

import com.jobradar.application.dto.AuthResponse;
import com.jobradar.application.dto.LoginRequest;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.exception.EmailAlreadyExistsException;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    public User register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); //USER-role auto

        return userRepository.save(user);

    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!passwordMatches){
            throw new BadCredentialsException("Invalid Email/Password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
