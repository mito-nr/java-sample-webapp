package com.example.webapp.controller;

import com.example.webapp.service.NewRelicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class HomeController {
    
    private final NewRelicService newRelicService;
    private final Random random = new Random();
    
    public HomeController(NewRelicService newRelicService) {
        this.newRelicService = newRelicService;
    }
    
    @GetMapping("/")
    public String home(@RequestParam(required = false) String userId, Model model) {
        // ユーザーIDを設定
        String effectiveUserId = userId != null ? userId : "user_" + random.nextInt(1000);
        newRelicService.setUserId(effectiveUserId);
        
        // カスタム属性を追加
        newRelicService.addCustomAttribute("page", "home");
        newRelicService.addCustomAttribute("visitor_type", "web");
        newRelicService.addCustomAttribute("request_count", random.nextInt(100));
        
        model.addAttribute("message", "Spring Boot Webアプリケーションへようこそ！");
        model.addAttribute("userId", effectiveUserId);
        return "index";
    }
    
    @GetMapping("/error-test")
    public String errorTest(@RequestParam(required = false) String userId, Model model) {
        // ユーザーIDを設定
        String effectiveUserId = userId != null ? userId : "user_" + random.nextInt(1000);
        newRelicService.setUserId(effectiveUserId);
        
        try {
            // 意図的にエラーを発生させる
            if (random.nextBoolean()) {
                throw new RuntimeException("テストエラー: ランダムに発生したエラーです");
            } else {
                throw new IllegalArgumentException("テストエラー: 不正な引数です");
            }
        } catch (Exception e) {
            // カスタム属性付きでエラーを記録
            Map<String, Object> errorAttributes = new HashMap<>();
            errorAttributes.put("error_page", "error-test");
            errorAttributes.put("user_id", effectiveUserId);
            errorAttributes.put("error_severity", "high");
            
            newRelicService.noticeError(e, errorAttributes);
            
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", effectiveUserId);
            return "error";
        }
    }
    
    @GetMapping("/custom-attributes")
    public String customAttributes(@RequestParam(required = false) String userId, Model model) {
        // ユーザーIDを設定
        String effectiveUserId = userId != null ? userId : "user_" + random.nextInt(1000);
        newRelicService.setUserId(effectiveUserId);
        
        // 複数のカスタム属性を一括追加
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("feature", "custom_attributes_demo");
        attributes.put("session_duration", random.nextInt(3600));
        attributes.put("is_premium", random.nextBoolean());
        attributes.put("region", "jp-tokyo");
        attributes.put("api_version", "v1.0");
        
        newRelicService.addCustomAttributes(attributes);
        
        model.addAttribute("message", "カスタム属性が追加されました");
        model.addAttribute("userId", effectiveUserId);
        model.addAttribute("attributes", attributes);
        return "custom";
    }
}
