package edu.asu.sbs;

import edu.asu.sbs.config.SbsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SbsApplication.class, SbsProperties.class})
public class SbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbsApplication.class, args);
    }

}
