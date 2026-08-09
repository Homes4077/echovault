package com.echovault.repository;

import com.echovault.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {
}
