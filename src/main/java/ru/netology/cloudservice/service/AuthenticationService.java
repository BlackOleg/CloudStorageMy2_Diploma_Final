package ru.netology.cloudservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.entity.Role;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.exception.InputDataException;
import ru.netology.cloudservice.model.EnumRoles;
import ru.netology.cloudservice.repository.AuthenticationRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.security.JwtTokenUtil;
import ru.netology.cloudservice.web.request.AuthRequest;
import ru.netology.cloudservice.web.response.AuthResponse;

import java.util.Collections;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationRepository authenticationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    The register method in the AuthenticationService class is responsible for registering a new user.
//    It checks if a user with the same username already exists in the database.
//    If not, it creates a new User object with the provided login and password, encodes the password
//    using a PasswordEncoder, assigns the ROLE_USER role to the user, and saves the user in the database.
      public ResponseEntity<?> register(AuthRequest request) {
        Optional<User> userFromBD = userRepository.findByUsername(request.getLogin());
        if (userFromBD.isPresent()) {
            throw new InputDataException("User with the same username already exists");
        }

        User newUser = User.builder()
                .username(request.getLogin())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(new Role(EnumRoles.ROLE_USER)))
                .build();
        userRepository.save(newUser);
        return ResponseEntity.ok(HttpStatus.OK);
    }

//Authenticate the user by calling the authenticate method of the AuthenticationManager with the username and password.
    public AuthResponse login(AuthRequest request) {
        final String username = request.getLogin();
        final String password = request.getPassword();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = userService.loadUserByUsername(username);
        String token = jwtTokenUtil.generateToken(userDetails);
        authenticationRepository.putTokenAndUsername(token, username);
        log.info("User {} is authorized", username);
        return AuthResponse.builder()
                .authToken(token)
                .build();
    }

// The method checks if the authToken starts with "Bearer ".
    public void logout(String authToken) {
        if (authToken.startsWith("Bearer ")) {
            authToken = authToken.substring(7);
        }
        final String username = authenticationRepository.getUserNameByToken(authToken);
        log.info("User {} logout", username);
        authenticationRepository.removeTokenAndUsernameByToken(authToken);
    }
}
