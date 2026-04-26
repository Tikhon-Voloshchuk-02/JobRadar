package com.jobradar.application.controller;

import com.jobradar.application.dto.AuthResponse;
import com.jobradar.application.dto.GoogleAuthRequest;
import com.jobradar.application.dto.LoginRequest;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.model.user.User;
import com.jobradar.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request){
        AuthResponse response= authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @RequestBody GoogleAuthRequest request
    ) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }


}
