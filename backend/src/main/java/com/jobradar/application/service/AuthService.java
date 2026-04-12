package com.jobradar.application.service;

import com.jobradar.application.dto.RegisterRequest;
import com.jobradar.application.exception.EmailAlreadyExistsException;
import com.jobradar.application.model.user.Role;
import com.jobradar.application.model.user.User;
import com.jobradar.application.model.user.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.USER); //USER-role auto

        return userRepository.save(user);

    }
}
