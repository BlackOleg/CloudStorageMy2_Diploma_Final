package ru.netology.cloudservice.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.netology.cloudservice.entity.Role;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.model.EnumRoles;
import ru.netology.cloudservice.repository.AuthenticationRepository;
import ru.netology.cloudservice.security.JwtTokenUtil;
import ru.netology.cloudservice.web.request.AuthRequest;
import ru.netology.cloudservice.web.response.AuthResponse;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing authentication service functionality.")
public class AuthenticationServiceTest {


    @InjectMocks
    AuthenticationService authenticationService;

    @Mock
    AuthenticationRepository authenticationRepository;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserService userService;

    @Mock
    JwtTokenUtil jwtTokenUtil;



    public static final String USERNAME = "anna@gmail.com";
    public static final String PASSWORD = "anna";
    public static final String TOKEN = "token";
    public static final String BEARER_TOKEN = "Bearer Token";
    public static final AuthResponse AUTHORIZATION_RESPONSE = new AuthResponse(TOKEN);
    public static final AuthRequest AUTHORIZATION_REQUEST = new AuthRequest(USERNAME, PASSWORD);


    @Test
    @DisplayName("Авторизация пользователя. Должно пройти успешно.")
    void login_Test() {
        //given

        Set<Role> roles = new HashSet<>();
        roles.add(new Role(EnumRoles.ROLE_ADMIN));
        UserDetails userDetails = new User(1L,"anna@gmail.com", "anna", roles);

        //when
        when(userService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn(TOKEN);

        //then
        AuthResponse expected = AUTHORIZATION_RESPONSE;
        AuthResponse result = authenticationService.login(AUTHORIZATION_REQUEST);
        assertEquals(expected,result);
        Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));
        Mockito.verify(authenticationRepository, Mockito.times(1)).putTokenAndUsername(TOKEN, USERNAME);
    }

    @Test
    @DisplayName("Logout. Должно пройти успешно.")
    void logout_Test() {

        when(authenticationRepository.getUserNameByToken(BEARER_TOKEN.substring(7))).thenReturn(USERNAME);
        authenticationService.logout(BEARER_TOKEN);
        Mockito.verify(authenticationRepository, Mockito.times(1)).getUserNameByToken(BEARER_TOKEN.substring(7));
        Mockito.verify(authenticationRepository, Mockito.times(1)).removeTokenAndUsernameByToken(BEARER_TOKEN.substring(7));

    }

}
