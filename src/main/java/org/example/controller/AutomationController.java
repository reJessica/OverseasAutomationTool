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
        return "automateAndDownloadFile Service End";
    }
}
