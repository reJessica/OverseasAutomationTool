package org.example.controller;


import org.example.service.SmartSolveAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/automation")
public class AutomationController {
    @Autowired
    private SmartSolveAutomationService smartSolveAutomationService;
    @GetMapping("/start")
    public String startSmartSolveAutomationService() {
        smartSolveAutomationService.automateAndDownloadFile();
        //定义一个方法 读取mysql中历史log并且和新download下来的文件进行比对 找出需要处理的record number 存入一个List中
        // todo 新建数据库 新建两张表 一张是log表 一张是每日的过滤以后的smartsolve 这里使用mybatis来实现
        //新download下来的数据需要filer 使用使用 Excel处理 相关的API
        // 显示需要处理的record number
        return "automateAndDownloadFile Service End";
    }

}
