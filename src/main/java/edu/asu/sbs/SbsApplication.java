package edu.asu.sbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SbsApplication.class})
public class SbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbsApplication.class, args);
    }

}
