package ru.netology.cloudservice.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.netology.cloudservice.entity.Role;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.model.EnumRoles;
import ru.netology.cloudservice.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing User service functionality.")
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("Получить пользователя по Username. Должно пройти успешно.")
    void loadUserByUsername_Test() {

        //given
        String username = "anna@gmail.com";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(EnumRoles.ROLE_ADMIN));
        User expected = new User(1L, "anna@gmail.com", "anna", roles);

        //when
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expected))
                .thenThrow(new UsernameNotFoundException(
                        format("User with username - %s, not found", username)));
        //then
        UserDetails result = userService.loadUserByUsername(username);
        assertEquals(expected,result);

    }
}
