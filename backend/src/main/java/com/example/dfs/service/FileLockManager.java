package com.example.dfs.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class FileLockManager {

    private final ConcurrentMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock lockFor(String key) {
        return lockMap.computeIfAbsent(key, unused -> new ReentrantLock());
    }
}
