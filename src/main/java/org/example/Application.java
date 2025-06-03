package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Application implements WebMvcConfigurer {
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/pages/**")
                .addResourceLocations("classpath:/static/pages/");
    }
}
