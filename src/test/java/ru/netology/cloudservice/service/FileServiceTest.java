package ru.netology.cloudservice.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.netology.cloudservice.entity.CloudFile;
import ru.netology.cloudservice.entity.Role;
import ru.netology.cloudservice.entity.User;
import ru.netology.cloudservice.mapper.FileMapper;
import ru.netology.cloudservice.model.EnumRoles;
import ru.netology.cloudservice.repository.AuthenticationRepository;
import ru.netology.cloudservice.repository.FileRepository;
import ru.netology.cloudservice.repository.UserRepository;
import ru.netology.cloudservice.web.response.FileWebResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестирование функционала {@link ru.netology.cloudservice.service.FileService}.
 */

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing File service functionality.")
public class FileServiceTest {

    @Spy
    @InjectMocks
    FileService fileService;

    @Mock
    AuthenticationRepository authenticationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    FileRepository fileRepository;

    @Mock
    FileMapper fileMapper;


    public static final String FILENAME = "file";
    public static final String FILENAME_2 = "file2";
    public static final String NEW_FILE_NAME = "new_file";
    public static final Optional<Long> USER_ID_OPTIONAL = Optional.of(1L);
    public static final Long USER_ID = USER_ID_OPTIONAL.get();
    public static final String BEARER_TOKEN = "Bearer Token";
    public static final String USERNAME = "anna@gmail.com";


    public static final MockMultipartFile MOCK_MULTIPART_FILE
            = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
    );

    public static CloudFile TEST_FILE = null;
    public static CloudFile TEST_FILE_2 = null;

    static {
        try {
            TEST_FILE = new CloudFile(FILENAME,
                    LocalDateTime.now(),
                    MOCK_MULTIPART_FILE.getContentType(),
                    MOCK_MULTIPART_FILE.getBytes(),
                    MOCK_MULTIPART_FILE.getSize(),
                    USER_ID);

            TEST_FILE_2 = new CloudFile(FILENAME_2,
                    LocalDateTime.now(),
                    MOCK_MULTIPART_FILE.getContentType(),
                    MOCK_MULTIPART_FILE.getBytes(),
                    MOCK_MULTIPART_FILE.getSize(),
                    USER_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CloudFile FILE_AFTER_EDIT_NAME = new CloudFile(NEW_FILE_NAME,
            TEST_FILE.getDate(),
            TEST_FILE.getType(),
            TEST_FILE.getFileData(),
            TEST_FILE.getSize(),
            TEST_FILE.getUserId());

    public static final List<CloudFile> CLOUD_FILE_LIST = List.of(TEST_FILE, TEST_FILE_2);


    @BeforeEach
    void setUp() {
        when(fileService.getUserIdFromToken(BEARER_TOKEN)).thenReturn(USER_ID_OPTIONAL);
    }


    @Test
    @DisplayName("Загрузить файл. Должно пройти успешно.")
    void uploadFile_Test() {

        //when
        when(fileRepository.findByUserIdAndFilename(USER_ID, FILENAME)).thenReturn(null);
        doNothing().when(fileService).uploadFile(BEARER_TOKEN, FILENAME, MOCK_MULTIPART_FILE);
        fileRepository.save(TEST_FILE);
        when(fileRepository.findByUserIdAndFilename(USER_ID, FILENAME)).thenReturn(TEST_FILE);

        //then
        fileService.uploadFile(BEARER_TOKEN, FILENAME, MOCK_MULTIPART_FILE);
        CloudFile uploadedFile = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);
        Mockito.verify(fileRepository, Mockito.times(1)).save(TEST_FILE);
        assertEquals(TEST_FILE, uploadedFile);

    }


    @Test
    @DisplayName("Удалить файл. Должно пройти успешно.")
    void deleteFile_Test() {

        doNothing().when(fileRepository).deleteByUserIdAndFilename(USER_ID, FILENAME);
        when(fileRepository.findByUserIdAndFilename(USER_ID, FILENAME)).thenReturn(null);

        fileService.deleteFile(BEARER_TOKEN, FILENAME);
        CloudFile result = fileRepository.findByUserIdAndFilename(USER_ID, FILENAME);
        Mockito.verify(fileRepository, Mockito.times(1)).deleteByUserIdAndFilename(USER_ID, FILENAME);
        assertNull(result);
    }

    @Test
    @DisplayName("Скачать файл. Должно пройти успешно.")
    void downloadFile_Test() {

        when(fileRepository.findByUserIdAndFilename(USER_ID, FILENAME)).thenReturn(TEST_FILE);

        CloudFile result = fileService.downloadFile(BEARER_TOKEN, FILENAME);
        assertEquals(TEST_FILE, result);
    }


    @Test
    @DisplayName("Изменить имя файла. Должно пройти успешно.")
    void editFile_Test() {

        when(fileRepository.findByUserIdAndFilename(USER_ID, NEW_FILE_NAME)).thenReturn(FILE_AFTER_EDIT_NAME);
        fileRepository.updateFileNameByUserId(USER_ID, FILENAME, NEW_FILE_NAME);

        fileService.editFile(BEARER_TOKEN, FILENAME, NEW_FILE_NAME);

        CloudFile result = fileRepository.findByUserIdAndFilename(USER_ID, NEW_FILE_NAME);
        assertEquals(NEW_FILE_NAME, result.getFilename());
    }


    @Test
    @DisplayName("Получить список всех файлов пользователя. Должно пройти успешно.")
    void getAllFiles_Test() {

        FileWebResponse fileWebResponse1 = new FileWebResponse(FILENAME, Math.toIntExact(TEST_FILE.getSize()));
        FileWebResponse fileWebResponse2 = new FileWebResponse(FILENAME_2, Math.toIntExact(TEST_FILE_2.getSize()));
        List<FileWebResponse> expectedList = List.of(fileWebResponse1, fileWebResponse2);

        when(fileRepository.findAllByUserIdWithLimit(USER_ID, 2)).thenReturn(CLOUD_FILE_LIST);
        when(fileMapper.cloudFileToFileWebResponse(TEST_FILE)).thenReturn(fileWebResponse1);
        when(fileMapper.cloudFileToFileWebResponse(TEST_FILE_2)).thenReturn(fileWebResponse2);

        List<FileWebResponse> resultList = fileService.getAllFiles(BEARER_TOKEN, 2);
        assertEquals(expectedList, resultList);

    }

    @Test
    @DisplayName("Получить ID пользователя из токена. Должно пройти успешно.")
    void getUserIdFromToken_Test() {

        Set<Role> roles = new HashSet<>();
        roles.add(new Role(EnumRoles.ROLE_ADMIN));
        Optional<User> user = Optional.of(new User(1L, "anna@gmail.com", "anna", roles));

        when(authenticationRepository.getUserNameByToken(BEARER_TOKEN)).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(user);
        when(fileService.getUserIdFromToken(BEARER_TOKEN)).thenReturn(ofNullable(user.get().getId()));

        Optional<Long> result = fileService.getUserIdFromToken(BEARER_TOKEN);
        assertEquals(ofNullable(user.get().getId()), result);

    }
}
