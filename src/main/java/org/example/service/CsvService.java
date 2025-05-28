package org.example.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface CsvService {
    void importCsv(MultipartFile file, String tableName) throws Exception;
    List<String> getAllTables();
    List<Map<String, Object>> getTableData(String tableName, int page, int size);
    long getTableCount(String tableName);
    List<Map<String, Object>> getTableStructure(String tableName);
} 