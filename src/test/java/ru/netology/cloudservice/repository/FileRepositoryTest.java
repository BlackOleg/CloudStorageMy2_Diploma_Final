package ru.netology.cloudservice.repository;


import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import ru.netology.cloudservice.config.SystemJpaTest;
import ru.netology.cloudservice.entity.CloudFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertDeleteCount;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Тесты репозитория FileRepository.
 * Количество селектов в assertSelectCount() зависит от того как мы запускаем тесты.
 * Если запускаем сразу весь класс со всеми тестами, то select из sequence выполнится только на тесте, который запустится первым.
 * Если запускать каждый тест по отдельности , то необходимо в  assertSelectCount() дописать +1 select из sequence
 * В данном случае  - запускать сразу целиком весь тестовый класс.
 */


@SystemJpaTest
public class FileRepositoryTest {

    @Autowired
    FileRepository fileRepository;

    public static final String FILENAME = "file";
    public static final String FILENAME_2 = "file2";
    public static final String NEW_FILE_NAME = "new_file";
    public static final Long USER_ID = 1L;
    public static final Optional<Long> USER_ID_OPTIONAL = Optional.of(1L);

    public static final MockMultipartFile TEST_FILE
            = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
    );

    public static CloudFile TEST_CLOUD_FILE = null;
    public static CloudFile TEST_CLOUD_FILE_2 = null;

    static {
        try {
            TEST_CLOUD_FILE = new CloudFile(FILENAME,
                    LocalDateTime.now(),
                    TEST_FILE.getContentType(),
                    TEST_FILE.getBytes(),
                    TEST_FILE.getSize(),
                    USER_ID);
            TEST_CLOUD_FILE_2 = new CloudFile(FILENAME_2,
                    LocalDateTime.now(),
                    TEST_FILE.getContentType(),
                    TEST_FILE.getBytes(),
                    TEST_FILE.getSize(),
                    USER_ID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }

    @DisplayName("Сохранение(загрузка) файла. Число insert должно равняться 1, select 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void insertFile_thenAssertDmlCount() {

        //when
        CloudFile savedFile = fileRepository.save(TEST_CLOUD_FILE);

        //then
        assertThat(savedFile.getFilename()).isEqualTo(FILENAME);
        assertSelectCount(1);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);


    }

    @DisplayName("Удаление файла. Число insert должно равняться 1, select 2, delete 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void deleteFile_thenAssertDmlCount() {

        //when
        CloudFile savedFile = fileRepository.save(TEST_CLOUD_FILE);
        fileRepository.deleteByUserIdAndFilename(USER_ID, FILENAME);
        CloudFile deletedFile = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);

        //then
        assertThat(savedFile.getFilename()).isEqualTo(FILENAME);
        assertThat(deletedFile).isNull();
        assertSelectCount(2);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(1);

    }

    @DisplayName("Скачивание файла. Число insert должно равняться 1, select 2")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void downloadFile_thenAssertDmlCount() {
        //when
        CloudFile savedFile = fileRepository.save(TEST_CLOUD_FILE);
        CloudFile downloadedFile = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);

        //then
        assertThat(savedFile.getFilename()).isEqualTo(FILENAME);
        assertThat(downloadedFile.getFilename()).isEqualTo(FILENAME);
        assertSelectCount(2);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);

    }

    @DisplayName("Изменение имени файла. Число insert должно равняться 1, select 2, update 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void updateFile_thenAssertDmlCount() {

        //when
        CloudFile savedFile = fileRepository.save(TEST_CLOUD_FILE);
        CloudFile beforeUpdate = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);
        fileRepository.updateFileNameByUserId(USER_ID, FILENAME, NEW_FILE_NAME);
        CloudFile updatedFileOld = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);
        CloudFile updatedFileNew = fileRepository.findByUserIdAndFilename(USER_ID, NEW_FILE_NAME);

        //then
        assertThat(savedFile.getFilename()).isEqualTo(FILENAME);
        assertThat(beforeUpdate.getFilename()).isEqualTo(FILENAME);
        Assertions.assertNull(updatedFileOld);
        assertThat(updatedFileNew.getFilename()).isEqualTo(NEW_FILE_NAME);
        assertSelectCount(4);
        assertInsertCount(1);
        assertUpdateCount(1);
        assertDeleteCount(0);

    }

    @DisplayName("Получение списка файлов. Число insert должно равняться 2, select 2")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_data.sql"
    })
    void getAllFiles_thenAssertDmlCount() {
        //given
        List<CloudFile> expectedList = new ArrayList<>();
        expectedList.add(TEST_CLOUD_FILE_2);
        expectedList.add(TEST_CLOUD_FILE);


        //when
        CloudFile savedFile1 = fileRepository.save(TEST_CLOUD_FILE);
        CloudFile savedFile2 = fileRepository.save(TEST_CLOUD_FILE_2);
        List<CloudFile> resultList = fileRepository.findAllByUserIdWithLimit(USER_ID, 2);

        //then
        assertThat(savedFile1.getFilename()).isEqualTo(FILENAME);
        assertThat(savedFile2.getFilename()).isEqualTo(FILENAME_2);
        Assertions.assertEquals(expectedList, resultList);
        assertSelectCount(2);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(0);

    }
}
