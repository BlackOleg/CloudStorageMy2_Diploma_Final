package ru.netology.cloudservice.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("ru.netology.cloudservice.entity")
@EnableJpaRepositories(basePackages = {"ru.netology.cloudservice.repository"})
@ComponentScan({"ru.netology.cloudservice.repository"})
public class SystemTestingJpaConfig {
}
