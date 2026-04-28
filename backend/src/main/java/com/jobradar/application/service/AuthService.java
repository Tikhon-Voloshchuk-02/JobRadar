package com.jobradar.application.service;

import com.jobradar.application.dto.AuthResponse;
import com.jobradar.application.dto.GoogleAuthRequest;
import com.jobradar.application.dto.LoginRequest;
import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.exception.EmailAlreadyExistsException;
import com.jobradar.application.exception.EmailNotVerifiedException;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;
import com.jobradar.application.repository.UserRepository;
import com.jobradar.application.security.JwtService;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class AuthService {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       GoogleTokenVerifierService googleTokenVerifierService,
                       EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.emailService = emailService;
    }

    @Transactional
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

        user.setEmailVerified(false);

        String token = UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        //check Email
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
        } catch (Exception e) {
          //  userRepository.delete(user);
            throw new RuntimeException("Failed to send verification email");
        }

        return user;
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

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        var payload = googleTokenVerifierService.verify(request.idToken());

        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        String fullName = name != null ? name : email;
        String[] parts = fullName.split(" ", 2);
        String firstname = parts[0];
        String lastname = parts.length > 1 ? parts[1] : "";

        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .map(existingUser -> {
                    existingUser.setGoogleId(googleId);
                    existingUser.setName(name);
                    existingUser.setPictureUrl(pictureUrl);

                    existingUser.setEmailVerified(true);

                    return userRepository.save(existingUser);
                })

                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setGoogleId(googleId);
                    newUser.setName(name);
                    newUser.setPictureUrl(pictureUrl);
                    newUser.setFirstname(firstname);
                    newUser.setLastname(lastname);
                    newUser.setPassword(passwordEncoder.encode("GOOGLE_AUTH_USER"));
                    newUser.setRole(Role.USER);

                    newUser.setEmailVerified(true);

                    return userRepository.save(newUser);
                });

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }


    /*Confirmation of the user's email by verification token.
    - user follows the link from the email --> system searches for the user by token
    --> The validity period --> is valid - email is marked as confirmed
     */
    @Transactional
    public void verifyEmail(String token){


        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification Token"));

        if(user.getEmailVerificationTokenExpiresAt()==null ||
                user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())){

            throw new RuntimeException("Verification Token expired =)");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);

        userRepository.save(user);
    }
}
