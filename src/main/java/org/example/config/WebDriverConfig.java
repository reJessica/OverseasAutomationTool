package org.example.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class WebDriverConfig {
    @Bean
    public WebDriver webDriver() {
        // 指定 ChromeDriver 的路径
        String driverPath = "C:\\Users\\z0052cmr\\Downloads\\chromedriver-win64-unzipped\\chromedriver-win64\\chromedriver.exe";
        File driverFile = new File(driverPath);
        if (driverFile.exists()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        } else {
            throw new IllegalStateException("The driver executable does not exist: " + driverPath);
        }

        ChromeOptions options = new ChromeOptions();
        // 添加一些常用的启动参数
        options.addArguments("--remote-allow-origins=*");//解决403报错问题（允许所有请求，避免在远程调试过程中出现WebSocket连接错误）
        return new ChromeDriver(options);
    }
}