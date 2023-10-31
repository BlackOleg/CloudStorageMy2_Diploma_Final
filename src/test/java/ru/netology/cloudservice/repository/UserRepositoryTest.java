package ru.netology.cloudservice.repository;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import ru.netology.cloudservice.config.SystemJpaTest;
import ru.netology.cloudservice.entity.User;

import java.util.Optional;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertDeleteCount;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * Тестирование функционала {@link ru.netology.cloudservice.repository.UserRepository}.
 */

@SystemJpaTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }

    @DisplayName("Получить юзера по username. Число select должно равняться 2, insert 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void findUserByUsername_thenAssertDmlCount() {

        String username = "tomas@gmail.com";

        User user = new User();
        user.setUsername("tomas@gmail.com");
        user.setPassword("tomas");

        User savedUser = userRepository.save(user);
        Optional<User> result = userRepository.findByUsername(username);
        assertThat(savedUser.getUsername()).isEqualTo(username);
        result.ifPresent(value -> assertThat(value.getUsername()).isEqualTo(username));
        assertSelectCount(2);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);


    }
}
