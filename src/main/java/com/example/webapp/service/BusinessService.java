package com.example.webapp.service;

import com.newrelic.api.agent.Trace;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BusinessService {
    
    private final Random random = new Random();
    private final NewRelicService newRelicService;
    
    public BusinessService(NewRelicService newRelicService) {
        this.newRelicService = newRelicService;
    }
    
    /**
     * ビジネスロジック処理（New Relicで計測）
     */
    @Trace(dispatcher = true)
    public String processOrder(String orderId, String userId) {
        newRelicService.addCustomAttribute("order_id", orderId);
        newRelicService.addCustomAttribute("processing_type", "order");
        
        // 処理時間をシミュレート
        simulateProcessing(100, 300);
        
        return "Order " + orderId + " processed successfully";
    }
    
    /**
     * データベース処理をシミュレート
     */
    @Trace(metricName = "Custom/Database/Query")
    public void queryDatabase(String query) {
        newRelicService.addCustomAttribute("db_query_type", "SELECT");
        newRelicService.addCustomAttribute("db_table", "orders");
        
        simulateProcessing(50, 150);
    }
    
    /**
     * 外部API呼び出しをシミュレート
     */
    @Trace(metricName = "Custom/ExternalAPI/Call")
    public String callExternalApi(String endpoint) {
        newRelicService.addCustomAttribute("api_endpoint", endpoint);
        newRelicService.addCustomAttribute("api_method", "GET");
        
        simulateProcessing(200, 500);
        
        return "API response from " + endpoint;
    }
    
    /**
     * 重い計算処理（コードでSpan計測を無効化）
     */
    @Trace(metricName = "Custom/Calculation/Heavy")
    public double heavyCalculation(int iterations) {
        // Spanを無効化
        com.newrelic.api.agent.NewRelic.getAgent().getTracedMethod().setMetricName((String[]) null);
        
        newRelicService.addCustomAttribute("calculation_iterations", iterations);
        
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.log(i + 1);
        }
        
        return result;
    }
    
    /**
     * エラーが発生する可能性のある処理
     */
    @Trace(metricName = "Custom/RiskyOperation")
    public void riskyOperation(String operationId) throws Exception {
        newRelicService.addCustomAttribute("operation_id", operationId);
        newRelicService.addCustomAttribute("risk_level", "high");
        
        simulateProcessing(100, 200);
        
        // 30%の確率でエラー
        if (random.nextInt(100) < 30) {
            throw new Exception("Operation failed: " + operationId);
        }
    }
    
    private void simulateProcessing(int minMs, int maxMs) {
        try {
            int delay = minMs + random.nextInt(maxMs - minMs);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
