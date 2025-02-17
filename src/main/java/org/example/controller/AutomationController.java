package org.example.controller;

import org.example.config.AppConfig;
import org.example.service.SmartSolveAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/automation")
public class AutomationController {
    @Autowired
    private SmartSolveAutomationService smartSolveAutomationService;

    @Autowired
    private AppConfig appConfig;

    @PostMapping("/config")
    public ResponseEntity<String> setConfig(@RequestBody Map<String, String> config) {
        appConfig.setTemplateFilePath(config.get("templateFilePath"));
        appConfig.setLogFilePath(config.get("logFilePath"));
        //appConfig.setOutputFilePath(config.get("outputFilePath"));
        return new ResponseEntity<>("配置已更新", HttpStatus.OK);
    }

    @GetMapping("/start")
    public List<List<String>> startSmartSolveAutomationService() {
        smartSolveAutomationService.automateAndDownloadFile();
        // todo 新建数据库 新建一张是log表 这里使用mybatis来实现 读取历史和写入新log记录
        List<List<String>> logList = smartSolveAutomationService.getLog();
        for (List<String> log : logList) {
            for (String line : log) {
                System.out.print(line + "\t");
            }
            System.out.println();
        }
        return logList;
    }
}