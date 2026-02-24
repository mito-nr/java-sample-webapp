package com.example.webapp.service;

import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NewRelicService {
    
    /**
     * エラーを記録
     */
    public void noticeError(Throwable throwable) {
        NewRelic.noticeError(throwable);
    }
    
    /**
     * メッセージ付きでエラーを記録
     */
    public void noticeError(String message) {
        NewRelic.noticeError(message);
    }
    
    /**
     * カスタム属性付きでエラーを記録
     */
    public void noticeError(Throwable throwable, Map<String, Object> attributes) {
        NewRelic.noticeError(throwable, attributes);
    }
    
    /**
     * ユーザーIDを設定
     */
    public void setUserId(String userId) {
        NewRelic.setUserId(userId);
    }
    
    /**
     * カスタム属性を追加
     */
    public void addCustomAttribute(String key, String value) {
        NewRelic.addCustomParameter(key, value);
    }
    
    /**
     * カスタム属性を追加（数値）
     */
    public void addCustomAttribute(String key, Number value) {
        NewRelic.addCustomParameter(key, value);
    }
    
    /**
     * カスタム属性を追加（真偽値）
     */
    public void addCustomAttribute(String key, boolean value) {
        NewRelic.addCustomParameter(key, value);
    }
    
    /**
     * 複数のカスタム属性を一括追加
     */
    public void addCustomAttributes(Map<String, Object> attributes) {
        attributes.forEach((key, value) -> {
            if (value instanceof String) {
                NewRelic.addCustomParameter(key, (String) value);
            } else if (value instanceof Number) {
                NewRelic.addCustomParameter(key, (Number) value);
            } else if (value instanceof Boolean) {
                NewRelic.addCustomParameter(key, (Boolean) value);
            }
        });
    }
}
