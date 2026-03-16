package com.example.webapp.service;

import com.newrelic.api.agent.NewRelic;
import com.sun.management.HotSpotDiagnosticMXBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.MBeanServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JvmMetricsService {
    
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    
    /**
     * 60秒ごとにJVMメトリクスを収集してNew Relicに送信
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void collectJvmMetrics() {
        try {
            // ヒープメモリ情報
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            
            // 非ヒープメモリ情報
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
            
            // クラスロード情報
            int loadedClassCount = classLoadingMXBean.getLoadedClassCount();
            long totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
            long unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
            
            // カスタムイベントとして送信
            Map<String, Object> attributes = new HashMap<>();
            
            // ヒープメトリクス
            attributes.put("heap.used", heapUsage.getUsed());
            attributes.put("heap.committed", heapUsage.getCommitted());
            attributes.put("heap.max", heapUsage.getMax());
            attributes.put("heap.init", heapUsage.getInit());
            attributes.put("heap.usedPercent", (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
            
            // 非ヒープメトリクス
            attributes.put("nonHeap.used", nonHeapUsage.getUsed());
            attributes.put("nonHeap.committed", nonHeapUsage.getCommitted());
            attributes.put("nonHeap.max", nonHeapUsage.getMax());
            
            // クラスロードメトリクス
            attributes.put("classes.loaded", loadedClassCount);
            attributes.put("classes.totalLoaded", totalLoadedClassCount);
            attributes.put("classes.unloaded", unloadedClassCount);
            
            // カスタムイベントとして記録
            NewRelic.getAgent().getInsights().recordCustomEvent("CustomJvmMetrics", attributes);
            
            System.out.println("JVM Metrics collected: " + 
                String.format("Heap: %.2f%%, Classes: %d", 
                    (double) heapUsage.getUsed() / heapUsage.getMax() * 100, 
                    loadedClassCount));
            
        } catch (Exception e) {
            System.err.println("Failed to collect JVM metrics: " + e.getMessage());
        }
    }
    
    /**
     * クラスごとのヒープ使用量を収集（120秒ごと）
     */
    @Scheduled(fixedRate = 120000, initialDelay = 20000)
    public void collectClassHistogram() {
        try {
            System.out.println("Collecting class histogram...");
            
            // jcmdを使用してクラスヒストグラムを取得
            String pid = getPid();
            if (pid == null) {
                System.err.println("Failed to get PID");
                return;
            }
            
            ProcessBuilder pb = new ProcessBuilder("jcmd", pid, "GC.class_histogram");
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int count = 0;
            int maxClasses = 20; // 上位20クラスのみ送信
            
            // ヘッダーをスキップ
            while ((line = reader.readLine()) != null && !line.contains("num     #instances")) {
                // skip
            }
            
            // クラスヒストグラムを解析
            Pattern pattern = Pattern.compile("\\s+(\\d+):\\s+(\\d+)\\s+(\\d+)\\s+(.+)");
            
            while ((line = reader.readLine()) != null && count < maxClasses) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String rank = matcher.group(1);
                    String instances = matcher.group(2);
                    String bytes = matcher.group(3);
                    String className = matcher.group(4);
                    
                    // クラスごとにイベントを送信
                    Map<String, Object> classAttributes = new HashMap<>();
                    classAttributes.put("className", className);
                    classAttributes.put("instances", Long.parseLong(instances));
                    classAttributes.put("bytes", Long.parseLong(bytes));
                    classAttributes.put("rank", Integer.parseInt(rank));
                    classAttributes.put("bytesInMB", Long.parseLong(bytes) / (1024.0 * 1024.0));
                    
                    NewRelic.getAgent().getInsights().recordCustomEvent("ClassHistogram", classAttributes);
                    
                    count++;
                }
            }
            
            process.waitFor();
            System.out.println("Class histogram collected: " + count + " classes");
            
        } catch (Exception e) {
            System.err.println("Failed to collect class histogram: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 現在のJavaプロセスのPIDを取得
     */
    private String getPid() {
        try {
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            return processName.split("@")[0];
        } catch (Exception e) {
            return null;
        }
    }
}
