package com.echovault.service;

import com.echovault.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminMonitoringService {

    @Autowired private UserRepository userRepository;
    @Autowired private VoiceNoteRepository voiceNoteRepository;
    @Autowired private LetterRepository letterRepository;
    @Autowired private GhostQueryRepository ghostQueryRepository;
    @Autowired private DeliveryLogRepository deliveryLogRepository;

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalUsers", userRepository.count());
        metrics.put("totalVoiceNotes", voiceNoteRepository.count());
        metrics.put("totalLetters", letterRepository.count());
        metrics.put("totalGhostQueries", ghostQueryRepository.count());
        metrics.put("totalDeliveries", deliveryLogRepository.count());
        return metrics;
    }
}
