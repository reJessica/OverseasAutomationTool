package org.example.controller;

import org.example.config.AppConfig;
import org.example.service.SmartSolveAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin
@RequestMapping("/automation")
public class AutomationController {
    private static final Logger logger = LoggerFactory.getLogger(AutomationController.class);

    @Autowired
    private SmartSolveAutomationService smartSolveAutomationService;

    @Autowired
    private AppConfig appConfig;

    @PostMapping("/config")
    public ResponseEntity<String> setConfig(@RequestBody Map<String, String> config) {
        try {
            logger.info("接收到配置更新请求: {}", config);
            appConfig.setTemplateFilePath(config.get("templateFilePath"));
            appConfig.setLogFilePath(config.get("logFilePath"));
            logger.info("配置更新成功");
            return new ResponseEntity<>("配置已更新", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("配置更新失败", e);
            return new ResponseEntity<>("配置更新失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/start")
    public ResponseEntity<?> startSmartSolveAutomationService() {
        try {
            logger.info("开始执行自动化服务");
            
            // 检查配置
            if (appConfig.getTemplateFilePath() == null || appConfig.getLogFilePath() == null) {
                String errorMsg = "配置信息不完整 - templateFilePath: " + appConfig.getTemplateFilePath() 
                    + ", logFilePath: " + appConfig.getLogFilePath();
                logger.warn(errorMsg);
                return ResponseEntity.badRequest().body(errorMsg);
            }

            // 检查文件是否存在
            java.nio.file.Path templatePath = java.nio.file.Paths.get(appConfig.getTemplateFilePath());
            java.nio.file.Path logPath = java.nio.file.Paths.get(appConfig.getLogFilePath());

            if (!java.nio.file.Files.exists(templatePath)) {
                String errorMsg = "模板文件不存在: " + templatePath;
                logger.error(errorMsg);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
            }

            if (!java.nio.file.Files.exists(logPath.getParent())) {
                String errorMsg = "日志文件目录不存在: " + logPath.getParent();
                logger.error(errorMsg);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
            }

            logger.info("使用配置 - templateFilePath: {}, logFilePath: {}", 
                       appConfig.getTemplateFilePath(), 
                       appConfig.getLogFilePath());

            try {
                // 执行服务
                smartSolveAutomationService.automateAndDownloadFile();
                List<List<String>> logs = smartSolveAutomationService.getLog();
                
                if (logs == null || logs.isEmpty()) {
                    logger.warn("服务执行完成，但日志为空");
                    return ResponseEntity.ok().body("服务执行完成，但没有生成日志");
                }
                
                logger.info("服务执行完成，返回 {} 条日志", logs.size());
                return ResponseEntity.ok(logs);
            } catch (Exception e) {
                String errorMsg = "自动化服务执行失败: " + e.getMessage();
                logger.error(errorMsg, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "服务启动过程中发生错误: " + e.getMessage();
            logger.error(errorMsg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopService() {
        try {
            logger.info("正在停止自动化服务");
            smartSolveAutomationService.stopService();
            logger.info("服务已成功停止");
            return ResponseEntity.ok("服务已停止");
        } catch (Exception e) {
            String errorMsg = "停止服务时发生错误: " + e.getMessage();
            logger.error(errorMsg, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            logger.info("正在获取服务状态");
            Map<String, Object> status = new HashMap<>();
            status.put("isRunning", smartSolveAutomationService.isRunning());
            status.put("processedCount", smartSolveAutomationService.getProcessedCount());
            status.put("lastUpdateTime", smartSolveAutomationService.getLastUpdateTime());
            logger.info("服务状态获取成功");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("获取服务状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/report-links")
    public ResponseEntity<List<Map<String, String>>> getReportLinks() {
        try {
            logger.info("正在获取报告链接");
            List<Map<String, String>> links = smartSolveAutomationService.getReportLinks();
            return ResponseEntity.ok(links);
        } catch (Exception e) {
            logger.error("获取报告链接失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}