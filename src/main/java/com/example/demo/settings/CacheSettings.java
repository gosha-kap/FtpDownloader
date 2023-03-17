package com.example.demo.settings;

import com.example.demo.entity.ExSettings;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

 public class CacheSettings {
    private static final Map<String, ExSettings> map = new ConcurrentHashMap<>();

    public static ExSettings save(ExSettings settings){
         return map.put(settings.getJobId(), settings);
    }

    public static ExSettings get(String key) {
       return map.get(key);
    }

    public static boolean exist(String key){
        return  map.containsKey(key);
    }

    public static boolean delete(String key){
         return Objects.nonNull(map.remove(key));
    }
}
