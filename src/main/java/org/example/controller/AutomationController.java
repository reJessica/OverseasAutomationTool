package org.example.controller;

import org.example.service.SmartSolveAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/automation")
public class AutomationController {
    private static final Logger logger = LoggerFactory.getLogger(AutomationController.class);

    @Autowired
    private SmartSolveAutomationService smartSolveAutomationService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("isRunning", smartSolveAutomationService.isRunning());
            status.put("processedCount", smartSolveAutomationService.getProcessedCount());
            status.put("lastUpdateTime", smartSolveAutomationService.getLastUpdateTime());
            
            logger.info("获取服务状态: {}", status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取服务状态失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 