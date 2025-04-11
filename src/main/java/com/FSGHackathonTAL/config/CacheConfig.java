package com.FSGHackathonTAL.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // Tạo các cache với cấu hình cụ thể
        ConcurrentMapCache userActiveCache = new ConcurrentMapCache("userActiveChat");
        ConcurrentMapCache doctorActiveCache = new ConcurrentMapCache("doctorActiveChat");
        ConcurrentMapCache chatSessionCache = new ConcurrentMapCache("chatSession");
        ConcurrentMapCache chatMessagesCache = new ConcurrentMapCache("chatMessages");
        
        cacheManager.setCaches(Arrays.asList(
            userActiveCache,
            doctorActiveCache,
            chatSessionCache,
            chatMessagesCache
        ));
        
        return cacheManager;
    }
    
    // Thêm bean để log thông tin về cache sử dụng
    @Bean
    public CacheMonitor cacheMonitor() {
        return new CacheMonitor();
    }
    
    // Class phụ trợ để giám sát cache
    public static class CacheMonitor {
        public void logCacheInfo(String cacheName, String operation, Object key) {
            System.out.println("Cache " + cacheName + ": " + operation + " - key: " + key);
        }
    }
}
