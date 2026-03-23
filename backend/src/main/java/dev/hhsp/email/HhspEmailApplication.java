package dev.hhsp.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HhspEmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(HhspEmailApplication.class, args);
    }
}
