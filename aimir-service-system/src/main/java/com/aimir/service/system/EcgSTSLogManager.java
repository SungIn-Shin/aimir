package com.aimir.service.system;

import java.util.List;
import java.util.Map;

public interface EcgSTSLogManager {
    
	public List<Map<String,Object>> getSTSHistory(Map<String,Object> condition);
	
}