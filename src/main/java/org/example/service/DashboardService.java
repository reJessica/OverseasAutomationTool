package org.example.service;

import java.util.Map;
 
public interface DashboardService {
    Map<String, Object> getStats();
    Map<String, Object> getActivities();
} 