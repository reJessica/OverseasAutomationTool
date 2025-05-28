package org.example.service.impl;

import org.example.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataServiceImpl implements DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private String getDatabaseName() {
        // 从URL中提取数据库名称
        String[] parts = dbUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.split("\\?")[0];
    }

    @Override
    public List<String> getAllTables() {
        try {
            String databaseName = getDatabaseName();
            String sql = "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = ? " +
                        "AND table_type = 'BASE TABLE' " +
                        "ORDER BY table_name";
            logger.info("执行SQL查询获取表列表: {}, 数据库: {}", sql, databaseName);
            List<String> tables = jdbcTemplate.queryForList(sql, String.class, databaseName);
            logger.info("查询到 {} 个表: {}", tables.size(), tables);
            return tables;
        } catch (Exception e) {
            logger.error("获取表列表时发生错误", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getTableStructure(String tableName) {
        try {
            String databaseName = getDatabaseName();
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = ? " +
                        "AND TABLE_NAME = ? " +
                        "ORDER BY ORDINAL_POSITION";
            logger.info("执行SQL查询获取表结构: {}, 数据库: {}, 表名: {}", sql, databaseName, tableName);
            List<Map<String, Object>> structure = jdbcTemplate.queryForList(sql, databaseName, tableName);
            logger.info("查询到 {} 个列", structure.size());
            return structure;
        } catch (Exception e) {
            logger.error("获取表结构时发生错误, 表名: " + tableName, e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getData(String tableName, int start, int length, String orderColumn, String orderDir, String search) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建基础查询
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM " + tableName);
        
        // 添加搜索条件
        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            // 获取表的所有列名
            List<Map<String, Object>> columns = getTableStructure(tableName);
            List<String> searchConditions = new ArrayList<>();
            
            for (Map<String, Object> column : columns) {
                String columnName = (String) column.get("COLUMN_NAME");
                searchConditions.add(columnName + " LIKE ?");
                params.add("%" + search + "%");
            }
            
            String whereClause = " WHERE " + String.join(" OR ", searchConditions);
            sql.append(whereClause);
            countSql.append(whereClause);
        }
        
        // 添加排序
        sql.append(" ORDER BY ").append(orderColumn).append(" ").append(orderDir);
        
        // 添加分页
        sql.append(" LIMIT ?, ?");
        params.add(start);
        params.add(length);
        
        // 执行查询
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        int total = jdbcTemplate.queryForObject(countSql.toString(), params.subList(0, params.size() - 2).toArray(), Integer.class);
        
        result.put("data", data);
        result.put("recordsTotal", total);
        result.put("recordsFiltered", total);
        
        return result;
    }

    @Override
    public Map<String, Object> getDataById(String tableName, Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    @Override
    public void deleteData(String tableName, Long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
} 