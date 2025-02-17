package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String templateFilePath;
    private String logFilePath;
    //private String outputFilePath;

    public String getTemplateFilePath() {
        return templateFilePath;
    }

    public void setTemplateFilePath(String templateFilePath) {
        this.templateFilePath = templateFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

//    public String getOutputFilePath() {
//        return outputFilePath;
//    }
//
//    public void setOutputFilePath(String outputFilePath) {
//        this.outputFilePath = outputFilePath;
//    }
}