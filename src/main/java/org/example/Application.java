package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.out.println("Start Springboot ...");
        String driverPath = System.getenv("CHROME_DRIVER_PATH");
        if (driverPath == null) {
            throw new IllegalStateException("The environment variable 'CHROME_DRIVER_PATH' is not set.");
        }
//        System.out.println(
//                driverPath
//        );
        SpringApplication.run(Application.class, args);
    }
}
