package com.mldm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
//@Import({HWDRWebConfiguration.class})
public class DigitRecognitionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitRecognitionApiApplication.class, args);
    }
}
