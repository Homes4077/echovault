package com.echovault.controller;

import com.echovault.repository.*;
import com.echovault.scheduler.DeliveryScheduler;
import com.echovault.scheduler.InactivityScheduler;
import com.echovault.service.AdminMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private AdminMonitoringService adminMonitoringService;
    @Autowired private UserRepository userRepository;
    @Autowired private DeliveryLogRepository deliveryLogRepository;
    @Autowired private GhostQueryRepository ghostQueryRepository;
    @Autowired private InactivityScheduler inactivityScheduler;
    @Autowired private DeliveryScheduler deliveryScheduler;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            model.addAttribute("metrics", adminMonitoringService.getSystemMetrics());
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("deliveryLogs", deliveryLogRepository.findAll());
            model.addAttribute("ghostQueries", ghostQueryRepository.findAll());
        } catch (Exception e) {
            System.err.println("ADMIN DASHBOARD ERROR: " + e.getMessage());
            e.printStackTrace();
            // Provide fallback empty structures if database queries fail
            model.addAttribute("users", Collections.emptyList());
            model.addAttribute("deliveryLogs", Collections.emptyList());
            model.addAttribute("ghostQueries", Collections.emptyList());
        }
        return "admin/dashboard";
    }

    @PostMapping("/demo/trigger-deliveries")
    @ResponseBody
    public String demoTriggerDeliveries() {
        int processed = deliveryScheduler.executePendingDeliveries();
        return "DEMO_SUCCESS: Processed " + processed + " time-locked letters via SendGrid.";
    }

    @PostMapping("/demo/trigger-inactivity")
    @ResponseBody
    public String demoTriggerInactivity() {
        int count = inactivityScheduler.executeInactivityChecks();
        return "DEMO_SUCCESS: Sent " + count + " inactivity alerts via Twilio SMS.";
    }
}
