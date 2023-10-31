package ru.netology.cloudservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;



@SpringBootApplication
public class CloudServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudServiceApplication.class, args);
    }
//    @Bean
//    public CommandLineRunner commandLineRunner(PasswordEncoder encoder) {
//        return args -> {
//            String password = "oleg";
//            String encryptedPassword = encoder.encode(password);
//            System.out.println("Encrypted password: " + encryptedPassword);
//
//        };
//    }

}
