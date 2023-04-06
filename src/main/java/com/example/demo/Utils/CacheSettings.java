package com.example.demo.Utils;

import com.example.demo.entity.DownloadSettings;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

 public class CacheSettings {
    private static final Map<Long, DownloadSettings> map = new ConcurrentHashMap<>();

    public static DownloadSettings save(DownloadSettings settings){
         return map.put(settings.getId(), settings);
    }

    public static DownloadSettings get(String key) {
       return map.get(key);
    }

    public static boolean exist(String key){
        return  map.containsKey(key);
    }

    public static boolean delete(String key){
         return Objects.nonNull(map.remove(key));
    }
}
