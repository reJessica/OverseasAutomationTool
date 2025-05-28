package org.example.controller;

import org.example.entity.Data;
import org.example.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    @Autowired
    private DataService dataService;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取数据库名称
            String[] parts = dbUrl.split("/");
            String lastPart = parts[parts.length - 1];
            String databaseName = lastPart.split("\\?")[0];
            
            // 获取表列表
            List<String> tables = dataService.getAllTables();
            
            response.put("status", "success");
            response.put("message", "数据库连接成功");
            response.put("database", databaseName);
            response.put("tables", tables);
            response.put("tableCount", tables.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "数据库连接失败: " + e.getMessage());
            response.put("error", e.toString());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/tables")
    public List<String> getTables() {
        return dataService.getAllTables();
    }

    @GetMapping("/table/{tableName}/structure")
    public ResponseEntity<?> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> structure = dataService.getTableStructure(tableName);
            return ResponseEntity.ok(structure);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/data")
    public ResponseEntity<?> getData(
            @RequestParam String tableName,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int length,
            @RequestParam(defaultValue = "id") String orderColumn,
            @RequestParam(defaultValue = "desc") String orderDir,
            @RequestParam(required = false) String search) {
        try {
            Map<String, Object> result = dataService.getData(tableName, start, length, orderColumn, orderDir, search);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/data/{tableName}/{id}")
    public ResponseEntity<?> getDataById(@PathVariable String tableName, @PathVariable Long id) {
        try {
            Map<String, Object> data = dataService.getDataById(tableName, id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/data/{tableName}/{id}")
    public ResponseEntity<?> deleteData(@PathVariable String tableName, @PathVariable Long id) {
        try {
            dataService.deleteData(tableName, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 