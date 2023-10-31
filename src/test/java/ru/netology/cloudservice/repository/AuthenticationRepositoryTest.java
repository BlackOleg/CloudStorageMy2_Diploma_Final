package ru.netology.cloudservice.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;



@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Testing authentication repository functionality.")
public class AuthenticationRepositoryTest {

    @Autowired
    AuthenticationRepository authenticationRepository;

    public static final String USERNAME = "oleg@gmail.com";
    public static final String TOKEN = "Token";

    @BeforeEach
    void setUp() {
        authenticationRepository = new AuthenticationRepository();
    }

    @Test
    @DisplayName("Сохранение токена и username.")
    void putTokenAndUsername_Test() {
        String nonExistentUserName = authenticationRepository.getUserNameByToken(TOKEN);
        String nonExistentToken = authenticationRepository.getTokenByUsername(USERNAME);
        assertNull(nonExistentUserName);
        assertNull(nonExistentToken);

        authenticationRepository.putTokenAndUsername(TOKEN, USERNAME);

        String userNameActual = authenticationRepository.getUserNameByToken(TOKEN);
        String tokenActual = authenticationRepository.getTokenByUsername(USERNAME);
        assertEquals(USERNAME, userNameActual);
        assertEquals(TOKEN, tokenActual);
    }


    @Test
    @DisplayName("Получение username по токену. Должно пройти успешно.")
    void getUserNameByToken_Test() {
        String nonExistentUserName = authenticationRepository.getUserNameByToken(TOKEN);
        assertNull(nonExistentUserName);

        authenticationRepository.putTokenAndUsername(TOKEN, USERNAME);
        String userNameActual = authenticationRepository.getUserNameByToken(TOKEN);
        assertEquals(USERNAME, userNameActual);
    }

    @Test
    @DisplayName("Удаление username и токена. Должно пройти успешно.")
    void removeTokenAndUsernameByToken_Test() {
        authenticationRepository.putTokenAndUsername(TOKEN, USERNAME);
        String userNameBeforeDeletion = authenticationRepository.getUserNameByToken(TOKEN);
        String tokenNameBeforeDeletion = authenticationRepository.getTokenByUsername(USERNAME);
        assertEquals(USERNAME, userNameBeforeDeletion);
        assertEquals(TOKEN, tokenNameBeforeDeletion);

        authenticationRepository.removeTokenAndUsernameByToken(TOKEN);
        String userNameActual = authenticationRepository.getUserNameByToken(TOKEN);
        String tokenActual = authenticationRepository.getTokenByUsername(USERNAME);
        assertNull(userNameActual);
        assertNull(tokenActual);

    }

    @Test
    @DisplayName("Получение токена по username. Должно пройти успешно.")
    void getTokenByUsername_Test() {
        String nonExistentToken = authenticationRepository.getTokenByUsername(USERNAME);
        assertNull(nonExistentToken);

        authenticationRepository.putTokenAndUsername(TOKEN, USERNAME);
        String tokenActual = authenticationRepository.getTokenByUsername(USERNAME);
        assertEquals(TOKEN, tokenActual);
    }
}
