package com.abhay.management.site.repository;

import com.abhay.management.site.entity.WorkUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkUpdateRepository extends JpaRepository<WorkUpdate, UUID> {

    List<WorkUpdate> findAllByTaskIdOrderByCreatedAtDesc(UUID taskId);

    List<WorkUpdate> findAllByUserId(UUID userId);
}
