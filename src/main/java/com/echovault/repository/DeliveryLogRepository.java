package com.echovault.repository;

import com.echovault.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
    List<DeliveryLog> findByUserId(Long userId);
}
