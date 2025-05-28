package org.example.service;

import java.util.List;
import java.util.Map;

public interface DataService {
    List<String> getAllTables();
    List<Map<String, Object>> getTableStructure(String tableName);
    Map<String, Object> getData(String tableName, int start, int length, String orderColumn, String orderDir, String search);
    Map<String, Object> getDataById(String tableName, Long id);
    void deleteData(String tableName, Long id);
} 